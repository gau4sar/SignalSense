package com.example.signalsense

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.CellSignalStrength
import android.telephony.SignalStrength
import android.util.Log
import cz.mroczis.netmonster.core.db.model.NetworkType
import cz.mroczis.netmonster.core.factory.NetMonsterFactory
import cz.mroczis.netmonster.core.model.cell.CellLte
import cz.mroczis.netmonster.core.model.cell.CellNr
import cz.mroczis.netmonster.core.model.cell.ICell

data class ICellWithNetworkType(
    val iCell: ICell,
    val alphaLong: String?, val networkType: NetworkType
)

data class ActiveSignalStrength(
    val rssi: String?,
    val rsrp: String?,
    val rsrq: String?,
    val snr: String?,
    val ssRsrp: String?,
    val ssRsrq: String?,
    val ssSinr: String?,
)

data class RegisteredCellIdWithAlphaLong(val cellId: Int, val alphaLong: String)

class NetMonsterHelper(private val context: Context) {

    // Callback interface
    interface NetworkInfoCallback {
        fun getCellList(cellList: List<ICellWithNetworkType>, ratTypes: String)
    }

    private var callback: NetworkInfoCallback? = null

    fun setCallback(callback: NetworkInfoCallback) {
        this.callback = callback
    }

    @SuppressLint("MissingPermission")
    fun getCellList(
        registeredCellsIdWithAlphaLong: MutableList<RegisteredCellIdWithAlphaLong>,
        signalStrength: SignalStrength
    ) {


        // Use the NetMonster library to collect network information
        val cellList = NetMonsterFactory.get(context).getCells()

        Log.d("signalsense", "cellList size -> " + cellList.size)

        val registeredCellIds = registeredCellsIdWithAlphaLong.map { it.cellId }


        // Filter cells
        val filteredLTECellsWithCellId = cellList.filterIsInstance<CellLte>().filter { cell ->
            cell.pci in registeredCellIds
        }

        val uniqueLTECells = filteredLTECellsWithCellId
            .groupBy { it.pci } // Group cells by cell ID
            .map { (_, cellList) ->
                // Sort cells in each group by the count of null values in signal and enb properties
                val sortedCells = cellList.sortedBy { cell ->
                    val nullValues = listOf(
                        cell.signal.rsrp,
                        cell.signal.rsrq,
                        cell.signal.snr,
                        cell.enb
                    ).count { it == null }
                    nullValues
                }
                // Choose the first cell from the sorted list (fewest null values)
                sortedCells.first()
            }

        val iCellLTEWithNetworkTypes = uniqueLTECells.map { iCell ->
            ICellWithNetworkType(
                iCell = iCell,
                alphaLong = registeredCellsIdWithAlphaLong.find { it.cellId == iCell.pci }?.alphaLong,
                NetMonsterFactory.get(context).getNetworkType(iCell.subscriptionId)
            )
        }


        // Filter NR cells
        val filteredNrCells = cellList.filterIsInstance<CellNr>().filter { cell ->
            cell.pci in registeredCellIds
        }

        // Initialize the ratTypeString
        var ratTypeString = ""

        // Check if there are filtered NR cells with NSA
        val hasNSA = filteredNrCells.any { cellNr ->
            NetMonsterFactory.get(context).getNetworkType(cellNr.subscriptionId) is NetworkType.Nr.Nsa
        }

        // Check if there are filtered NR cells with SA
        val hasSA = filteredNrCells.any { cellNr ->
            NetMonsterFactory.get(context).getNetworkType(cellNr.subscriptionId) is NetworkType.Nr.Sa
        }

        
        
        // remove nsa from list
        val filteredNrCellsWithOnlySa = filteredNrCells.filter {cellNr ->
            NetMonsterFactory.get(context).getNetworkType(cellNr.subscriptionId) is NetworkType.Nr.Sa
        }
        

        val iCellSAWithNetworkType = filteredNrCellsWithOnlySa.map { iCell ->
            ICellWithNetworkType(
                iCell = iCell,
                alphaLong = registeredCellsIdWithAlphaLong.find { it.cellId == iCell.pci }?.alphaLong,
                NetMonsterFactory.get(context).getNetworkType(iCell.subscriptionId)
            )
        }


        // Combine filtered LTE and NR cells
        val filteredCells = iCellLTEWithNetworkTypes + iCellSAWithNetworkType

        Log.d(
            "signalsense",
            "cellList size-> " + cellList.size + " filteredCells->" + filteredLTECellsWithCellId.size
        )


        if (hasNSA) {
            if (ratTypeString.isNotEmpty()) {
                ratTypeString += ", "
            }
            ratTypeString += "5G NSA"
        }

        if(!hasNSA) {
            // Check if there are filtered NR cells with NSA
            val hasLteWithNSA = filteredLTECellsWithCellId.any { cellLte ->
                NetMonsterFactory.get(context)
                    .getNetworkType(cellLte.subscriptionId) is NetworkType.Nr.Nsa
            }

            // Add "NSA" to ratTypeString if it's not already present
            if (hasLteWithNSA) {
                Log.d("signalsense","hasLteWithNSA -> "+hasLteWithNSA)
                if (ratTypeString.isNotEmpty()) {
                    ratTypeString += ", "
                }
                ratTypeString += "5G NSA"
            }
            else{
                if (ratTypeString.isNotEmpty()) {
                    ratTypeString += ", "
                }
                ratTypeString += "LTE"
            }
        }

        if (hasSA) {
            if (ratTypeString.isNotEmpty()) {
                ratTypeString += ", "
            }
            ratTypeString += "SA"
        }

        if(filteredLTECellsWithCellId.isEmpty() && filteredNrCells.isEmpty()){
            ratTypeString = ""
        }

        callback?.getCellList(filteredCells, ratTypeString)
    }
}
