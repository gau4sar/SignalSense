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
    val cellLte: CellLte,
    val alphaLong: String?,
    val networkType: NetworkType,
    val ratType: String,
    val _5gSignals: CellNr? = null
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
        activeSignalStrength: ActiveSignalStrength
    ) {


        // Use the NetMonster library to collect network information
        val cellList = NetMonsterFactory.get(context).getCells()

        Log.d("signalsense", "cellList size -> " + cellList.size)
        Log.d("signalsense", "NetMonsterFactory cellList" + cellList)

        val registeredCellIds = registeredCellsIdWithAlphaLong.map { it.cellId }


        // Filter LTE cells
        val filteredLTECellsWithCellId = cellList.filterIsInstance<CellLte>().filter { cell ->
            cell.pci in registeredCellIds
        }
        Log.d(
            "signalsense",
            "NetMonsterFactory filteredLTECellsWithCellId" + filteredLTECellsWithCellId
        )

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


        // Filter NR cells
        val filteredNrCells = cellList.filterIsInstance<CellNr>().filter { cell ->
            cell.pci in registeredCellIds
        }.toMutableList()


        val uniqueNrCells = filteredNrCells
            .groupBy { it.pci } // Group cells by cell ID
            .map { (_, cellList) ->
                // Sort cells in each group by the count of null values in signal and enb properties
                val sortedCells = cellList.sortedBy { cell ->
                    val nullValues = listOf(
                        cell.signal.ssRsrp,
                        cell.signal.ssRsrq,
                        cell.signal.ssSinr,
                    ).count { it == null }
                    nullValues
                }
                // Choose the first cell from the sorted list (fewest null values)
                sortedCells.first()
            }.toMutableList()


        val finalAddedCells = mutableListOf<ICellWithNetworkType>()

        for (lteCell in uniqueLTECells) {
            var iCellWithNetworkType: ICellWithNetworkType? = null

            val matchingNSaCell = uniqueNrCells.find {
                it.pci == lteCell.pci
            }

            val networkType =
                NetMonsterFactory.get(context).getNetworkType(lteCell.subscriptionId)


            /*Log.d(
                "signalsense",
                "NetMonsterFactory networkType" + networkType
            )*/


            if (matchingNSaCell != null) {

                if (matchingNSaCell.signal.ssRsrp == null || matchingNSaCell.signal.ssRsrq == null || matchingNSaCell.signal.ssSinr == null) {

                    iCellWithNetworkType = ICellWithNetworkType(
                        cellLte = lteCell,
                        networkType = networkType,
                        alphaLong = registeredCellsIdWithAlphaLong.find { it.cellId == lteCell.pci }?.alphaLong,
                        ratType = getRatType(networkType)
                    )
                } else {

                    // Remove the matching NSA cell
                    uniqueNrCells.remove(matchingNSaCell)

                    iCellWithNetworkType = ICellWithNetworkType(
                        cellLte = lteCell,
                        networkType = networkType,
                        alphaLong = registeredCellsIdWithAlphaLong.find { it.cellId == lteCell.pci }?.alphaLong,
                        ratType = RatType._5G_NSA.value,
                        _5gSignals = matchingNSaCell
                    )
                }
            } else {
                iCellWithNetworkType = ICellWithNetworkType(
                    cellLte = lteCell,
                    networkType = networkType,
                    alphaLong = registeredCellsIdWithAlphaLong.find { it.cellId == lteCell.pci }?.alphaLong,
                    ratType = getRatType(networkType)
                )
            }

            finalAddedCells.add(iCellWithNetworkType)
        }

        Log.d("signalsense", "NetMonsterFactory filteredNrCells" + filteredNrCells)


        // Initialize the ratTypeList
        val ratTypeList = mutableListOf<String>()

        finalAddedCells.map {
            ratTypeList += it.ratType
        }

        var ratTypeString = ""

        if (filteredLTECellsWithCellId.isEmpty() && filteredNrCells.isEmpty()) {
            ratTypeList.clear()
        } else {
            ratTypeString = ratTypeList.distinct().joinToString(", ")
        }

        callback?.getCellList(finalAddedCells, ratTypeString)
    }

    private fun getRatType(networkType: NetworkType): String {
        return when (networkType) {
            is NetworkType.Nr.Nsa -> {
                RatType._5G_NSA.value
            }

            is NetworkType.Lte -> {
                RatType.LTE.value
            }

            is NetworkType.Nr.Sa -> {
                RatType._5G_SA.value
            }

            else -> {
                RatType.N_A.value
            }
        }
    }


    enum class RatType(val value: String) {
        LTE("LTE"),
        _5G_NSA("5G NSA"), // Note: Using underscore for the enum variant with spaces
        _5G_SA("5G SA"),
        N_A("N/A"); // Default value if not recognized
    }

}
