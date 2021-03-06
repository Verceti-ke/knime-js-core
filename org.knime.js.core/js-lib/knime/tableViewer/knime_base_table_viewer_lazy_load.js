/* eslint-env es6, jquery */
/* eslint no-var: "error" */
window.KnimeBaseTableViewer.prototype._lazyLoadData = function (data, callback, settings) {
    // TODO: evaluation needs to take into account order, filter, search
    const win = [data.start, data.start + data.length - 1];
    if (this._knimeTable) {
        const cacheStart = this._knimeTable.getFragmentFirstRowIndex();
        const cached = [cacheStart, cacheStart + this._knimeTable.getNumRows() - 1];
        const included = cached[0] <= win[0] && cached[1] >= win[1];
        if (included) {
            this._lazyLoadResponse(data, callback);
        } else {
            const request = {
                start: data.start,
                length: data.length,
                search: data.search,
                order: data.order,
                columns: data.columns
            };
            const self = this;
            let processingPanel = this._getJQueryTableContainer().find('.dataTables_processing');
            processingPanel.text('Processing...').prop('title', '');
            if (this._runningRequest) {
                this._runningRequest.cancel();
            }
            let promise = knimeService.requestViewUpdate(request);
            this._runningRequest = promise;
            promise.progress(monitor => {
                processingPanel.prop('title', monitor.progressMessage ? monitor.progressMessage : '');
                if (monitor.progress) {
                    let percent = (monitor.progress * 100).toFixed(0);
                    processingPanel.text('Processing... (' + percent + '%)');
                }
            }).then(response => {
                if (response.error) {
                    self._lazyLoadResponse(data, callback, response.error);
                } else {
                    self._knimeTable.mergeTables(response.table);
                    self._lazyLoadResponse(data, callback);
                }
            }).catch(error => {
                knimeService.logError(error);
                alert('Could not load data: ' + error);
                self._lazyLoadResponse(data, callback, error);
            });
        }
    }
};

window.KnimeBaseTableViewer.prototype._lazyLoadResponse = function (data, callback, error) {
    this._runningRequest = null;
    let response = {
        draw: data.draw
    };
    response.recordsTotal = this._knimeTable.getTotalRowCount();
    response.recordsFiltered = this._knimeTable.getTotalFilteredRowCount();
    if (error) {
        response.error = error;
        response.data = [];
    } else {
        let firstRow = data.start - this._knimeTable.getFragmentFirstRowIndex();
        let lastRow = firstRow + data.length;
        response.data = this._getDataSlice(firstRow, lastRow);
    }
    callback(response);
};
