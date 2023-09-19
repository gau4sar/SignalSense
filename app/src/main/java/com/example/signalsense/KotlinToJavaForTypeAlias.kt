package com.example.signalsense

import cz.mroczis.netmonster.core.model.cell.ICell
import cz.mroczis.netmonster.core.model.model.CellError

interface CellCallbackSuccess {
    fun onSuccess(cells: List<ICell>)
}

interface CellCallbackError {
    fun onError(error: CellError)
}
