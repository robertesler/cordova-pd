/***
 Your friendly angular wrapper for cordova-pd, ng-cordova-pd

 These functions will have to be called using the following method
 $puredata.receiveFloat("sendName").then(function (result) {
 // success
 }, function (err) {
 // error
 });
 
 Copyright 2016
 Robert Esler
 ***/


angular.module('ngPd', [])

.factory('$puredata', ['$q', '$window', function ($q, $window) {

    
    return {
                //Send data to Pd
                sendFloat: function (receiveName, theFloat) {
                       var d = $q.defer();
                    
                       $window.plugins.pd.sendFloat(receiveName, theFloat, function (message) {
                                                    d.resolve(message);
                                                    }, function (error) {
                                                    d.reject(error);
                                                    });
                       
                       return d.promise;
                },
                    
                sendMessage: function (receiveName, theMessage, theList) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.sendMessage(receiveName, theMessage, theList, function (message) {
                                                    d.resolve(message);
                                                    }, function (error) {
                                                    d.reject(error);
                                                    });
                       
                       return d.promise;
                       },
                
                sendBang: function (receiveName) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.sendBang(receiveName, function (message) {
                                                    d.resolve(message);
                                                    }, function (error) {
                                                    d.reject(error);
                                                    });
                       
                       return d.promise;
                       },
                       
                sendSymbol: function (receiveName, theSymbol) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.sendSymbol(receiveName, theSymbol, function (message) {
                                                    d.resolve(message);
                                                    }, function (error) {
                                                    d.reject(error);
                                                    });
                       
                       return d.promise;
                       },
                       
                sendList: function (receiveName, theList) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.sendList(receiveName, theList, function (message) {
                                                    d.resolve(message);
                                                    }, function (error) {
                                                    d.reject(error);
                                                    });
                       
                       return d.promise;
                       },
            
               //Receive data from Pd
                      
                       
                receiveBang: function (sendName) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.receiveBang(sendName, function (message) {
                                                   d.resolve(message);
                                                   }, function (error) {
                                                   d.reject(error);
                                                   });
                       
                       return d.promise;
                       },
                       
                receiveFloat: function (sendName) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.receiveFloat(sendName, function (message) {
                                                      d.resolve(message);
                                                      }, function (error) {
                                                      d.reject(error);
                                                      });
                       
                       return d.promise;
                       },
                       
                receiveSymbol: function (sendName) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.receiveSymbol(sendName, function (message) {
                                                      d.resolve(message);
                                                      }, function (error) {
                                                      d.reject(error);
                                                      });
                       
                       return d.promise;
                       },
                       
                receiveList: function (sendName) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.receiveList(sendName, function (message) {
                                                        d.resolve(message);
                                                        }, function (error) {
                                                        d.reject(error);
                                                        });
                       
                       return d.promise;
                       },
                       
                receiveMessage: function (sendName) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.receiveMessage(sendName, function (message) {
                                                      d.resolve(message);
                                                      }, function (error) {
                                                      d.reject(error);
                                                      });
                       
                       return d.promise;
                       },
                       
                echo: function (str, callback) {
                       var d = $q.defer();
                       
                       $window.plugins.pd.receiveMessage(str, function (message) {
                                                         d.resolve(message);
                                                         }, function (error) {
                                                         d.reject(error);
                                                         });
                       
                       return d.promise;
                       }

                       
    }; //return
                
}]);
