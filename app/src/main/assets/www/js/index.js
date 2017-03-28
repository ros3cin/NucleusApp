function parseJson(stringResponse){
    var coordinates = [];
    var values = [];
    var statuses = [];
    var ids = [];
    alert(stringResponse)
    var json = JSON.parse(stringResponse);
    var ctxResponses = json["contextResponses"];
    for(var i = 0; i < ctxResponses.length; i++){
        ids = ids.concat(ctxResponses[i]["contextElement"]["id"]);
        coordinates = coordinates.concat(ctxResponses[i]["contextElement"]["attributes"][0]["value"]);
        values = values.concat(parseFloat(ctxResponses[i]["contextElement"]["attributes"][1]["value"]));
        statuses = statuses.concat(ctxResponses[i]["contextElement"]["attributes"][2]["value"]);
    }
    Android.receiveJSData(coordinates,values,statuses,ids);
}