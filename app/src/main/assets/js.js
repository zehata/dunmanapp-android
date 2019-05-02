var currentappid = "";
var config = {
    apiKey: "AIzaSyDfAkKocOAk3Elv9V5x0Po4NXuuGGCfdng",
    authDomain: "dhypetest0.firebaseapp.com",
    databaseURL: "https://dhypetest0.firebaseio.com",
    projectId: "dhypetest0",
    storageBucket: "",
    messagingSenderId: "677082806183"
};
firebase.initializeApp(config);
var dataserver;
var currentappdata = "";
var appver = 0;
var applist = [];
var installedapplist;

function $(name) {
    return document.getElementById(name);
}

function refreshapplist() {
    installedapplist = [];
    if (localStorage.getItem('applist')) {
        installedapplist = JSON.parse(localStorage.getItem('installedapplist'))
        applist = JSON.parse(localStorage.getItem('applist'));
        appver = Number(localStorage.getItem('CurrentVersion'));
    }
    updatecheck();
    $('applist').innerHTML = "";
    for (var i = 0; i < applist.length; i++) {
        var newdiv = document.createElement('div');
        newdiv.className = "applistentry";
        newdiv.innerHTML = "<div onclick='goto(" + i + ")'>" + applist[i][0] + "</div>";
        $('applist').appendChild(newdiv);
    }
}
refreshapplist();

var selectedapp;
function goto(x) {
	var selectedappid = applist[x][2];
	selectedapp = JSON.parse(localStorage.getItem(selectedappid));

	var webpagetodisplay = '<script type="text/javascript">function getappdata(){return parent.sendappdata();}function newpage() {this.location = "";}const pagechange = {set(obj, prop, value) {if (prop === "location") {parent.gotopage(value);}}}const newpage1 = new newpage();const nextpage = new Proxy(newpage1, pagechange); </sc' + 'ript>' + selectedapp["index?html"]; // injecting data adaptor

    var dataserver = firebase.database().ref('/appsstorage/' + selectedappid + '/');

    closedrawer();

    dataserver.once("value", function(snapshot) {
        currentappdata = snapshot.val();
        $('miniapp').src = "about:blank";
        setTimeout(function() { $('miniapp').contentDocument.write(webpagetodisplay); }, 1);
        closedrawer();
    }); // finishing loading of index
}

function gotopage(html) {
    html = html.replace(new RegExp("\\.", "g"), "?");
    var apppage = selectedapp[html];
    var webpagetodisplay = '<script type="text/javascript">function getappdata(){return parent.sendappdata();}function newpage() {this.location = "";}const pagechange = {set(obj, prop, value) {if (prop === "location") {parent.gotopage(value);}}}const newpage1 = new newpage();const nextpage = new Proxy(newpage1, pagechange); </sc' + 'ript>' + apppage;
    $('miniapp').src = "about:blank";
    setTimeout(function() { $('miniapp').contentDocument.write(webpagetodisplay); }, 1);
}

function sendappdata() {
    return currentappdata;
}

function updatecheck() {
    appverserver = firebase.database().ref('appsversion/');
    if (installedapplist && installedapplist.length > 0) {
        appverserver.on("value", function(snapshot) { // check the database for app versions and app list version
            var rawdata = Object.entries(snapshot.val());
            if (appver < Number(snapshot.val().CurrentVersion)) { // if there has been an update to the app list version
                // for each installed app
                for (var j = 0; j < installedapplist.length; j++) {
                    // should we update or delete or leave it?
                    // should we delete?
                    var deleteapp = true;
                    // does it exist in app server (not applist)
                    for (var i = 0; i < rawdata.length; i++) {
                        if (rawdata[i][0] != "applist" && rawdata[i][0] != "CurrentVersion") {
                            if (installedapplist[j][0] == rawdata[i][0]) {
                                deleteapp = false;
                            }
                        }
                    }
                    if (deleteapp == true) {
                        // delete this app
                        localStorage.removeItem(installedapplist[j][0]);
                    }
                    // should we update?
                    for (var i = 0; i < rawdata.length; i++) {
                        if (rawdata[i][0] != "applist" && rawdata[i][0] != "CurrentVersion") {
                            if (installedapplist[j][0] == rawdata[i][0]) {
                                // if server version is higher
                                if (Number(installedapplist[j][1]) < rawdata[i][1]) {
                                    // then update
                                    firebase.database().ref('apps/' + rawdata[i][0] + '/').once("value").then(function(snapshot) {
                                        localStorage.setItem(installedapplist[j][0], snapshot.val());
                                    });
                                }
                            }
                        }
                    }
                }
                // for each app in appserver
                for (var k = 0; k < rawdata.length; k++) {
                    if (rawdata[k][0] != "applist" && rawdata[k][0] != "CurrentVersion") {
                        // check if it is installed
                        var installed = false;
                        for (var l = 0; l < installedapplist.length; l++) {
                            if (installedapplist[l][0] == rawdata[k][0]) {
                                installed = true;
                            }
                        }
                        if (installed == false) {
                            var savingappid = rawdata[k][0];
                            firebase.database().ref('apps/' + savingappid + '/').once("value").then(function(snapshot) {
                            console.log(savingappid)
                                localStorage.setItem(savingappid, JSON.stringify(snapshot.val()));
                            });
                        }
                    }
                }
            }
        });
    } else {
        appverserver.once("value").then(function(snapshot) {
            localStorage.setItem("applist", JSON.stringify(snapshot.val().applist));
            appver = Number(snapshot.val().CurrentVersion);
            localStorage.setItem('CurrentVersion',appver);
        })
        firebase.database().ref('apps/').once("value").then(function(snapshot) {
            var updatedapps = Object.entries(snapshot.val());
            for (var i = 0; i < updatedapps.length; i++) {
                var installedapplist = [];
                installedapplist.push([updatedapps[i][0], updatedapps[i][1]["version"]]);
                localStorage.setItem(updatedapps[i][0], JSON.stringify(updatedapps[i][1]));
            }
            localStorage.setItem('installedapplist', JSON.stringify(installedapplist));
        	refreshapplist();
        });
    }
}
// TODO: Rewrite these
// function updatecheck(){
// 	appverserver = firebase.database().ref('appsversion/');
// 	appverserver.on("value",function(snapshot){
// 		if (appver < Number(snapshot.val().CurrentVersion)){
// 			//clear other apps
// 			for(var installedapp in installedapplist){
// 				localStorage.remove(installedapplist[installedapp]);
// 			}
// 			updateapps();
// 			localStorage.setItem("CurrentVersion",snapshot.val().CurrentVersion);
// 		}
// 		localStorage.setItem("applist",JSON.stringify(snapshot.val().applist));
// 		refreshapplist();
// 	});
// }
// function updateapps(){
// 	appserver = firebase.database().ref('apps/');
// 	var installedapplist = [];
// 	appserver.once("value",function(snapshot){
// 		var updatedapps = Object.entries(snapshot.val());
// 		for(var i = 0; i < updatedapps.length; i++){
// 			installedapplist.push(updatedapps[i][0]);
// 			localStorage.setItem(updatedapps[i][0],"[\""+updatedapps[i][1][0]+"\","+updatedapps[i][1][1]+"]");
// 		}
// 		localStorage.setItem('installedapplist',JSON.stringify(installedapplist))
// 	});
// }
function opendrawer() {
    $("darken").style.zIndex = "2";
    setTimeout(function() { $('darken').style.backgroundColor = "black"; }, 100);
    swipeRevealItems[0].changeState(1);
}

function closedrawer() {
    $('darken').style.backgroundColor = "transparent";
    setTimeout(function() { $("darken").style.zIndex = "-1"; }, 100);
    swipeRevealItems[0].changeState(2);
}

if (!Object.entries)
  Object.entries = function( obj ){
    var ownProps = Object.keys( obj ),
        i = ownProps.length,
        resArray = new Array(i); // preallocate the Array
    while (i--)
      resArray[i] = [ownProps[i], obj[ownProps[i]]];
    
    return resArray;
  };

// reccards adaptor V

var recAlgoServer = firebase.database().ref('/reccards/algo/');
var recManualServer = firebase.database().ref('/reccards/manual/');
var recAlgoData = {};
recAlgoServer.once("value",function(snapshot){
    recAlgoData = snapshot.val();
});

if(recServerManual()){
    if(recServerManualUpdated()){

    } else {
        recDeleteOutdated()
        checkRecServerAlgoUpdated();
    }
} else {
    checkRecServerAlgoUpdated();
}

function checkRecServerAlgoUpdated(){
    if(recServerAlgoUpdated()){
        updateRecAlgo();
    } else {
        useRecAlgo();
    }
}

function recServerManualUpdated(){

}

function recDeleteOutdated(){

}

function recServerAlgoUpdated(){
    var recserverversion = 0;
    if (localStorage.getItem('recAlgo')){
        recserverversion = JSON.parse(localStorage.getItem('recAlgo')).version;
    }
    if(recserverversion < recAlgoData.version){
        return true;
    }
    return false;
}

function updateRecAlgo(){
    localStorage.setItem('recAlgo',JSON.stringify(recAlgoData));
}

function useRecAlgo(){
    var recAlgo = JSON.parse(localStorage.getItem('recAlgo'));
    var recAlgoPro = Object.entries(recAlgo);
    for(var i = 0; i < recAlgoPro.length; i++){
        if(recAlgoPro[0] != "version"){
            if(eval(recAlgoPro[1][0])){
                var reply = eval(recAlgoPro[1][1]);
            }
        }
    }
}