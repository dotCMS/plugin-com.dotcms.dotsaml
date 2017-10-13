System.register(['../services/core-web-service', '@angular/http', '@angular/core', 'rxjs/Rx', '@angular/router', 'rxjs/Subject', './dotcms-events-service', './logger.service'], function(exports_1, context_1) {
    "use strict";
    var __moduleName = context_1 && context_1.id;
    var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
        var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
        if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
        else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
        return c > 3 && r && Object.defineProperty(target, key, r), r;
    };
    var __metadata = (this && this.__metadata) || function (k, v) {
        if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
    };
    var core_web_service_1, http_1, core_1, Rx_1, router_1, Subject_1, dotcms_events_service_1, logger_service_1;
    var LoginService;
    return {
        setters:[
            function (core_web_service_1_1) {
                core_web_service_1 = core_web_service_1_1;
            },
            function (http_1_1) {
                http_1 = http_1_1;
            },
            function (core_1_1) {
                core_1 = core_1_1;
            },
            function (Rx_1_1) {
                Rx_1 = Rx_1_1;
            },
            function (router_1_1) {
                router_1 = router_1_1;
            },
            function (Subject_1_1) {
                Subject_1 = Subject_1_1;
            },
            function (dotcms_events_service_1_1) {
                dotcms_events_service_1 = dotcms_events_service_1_1;
            },
            function (logger_service_1_1) {
                logger_service_1 = logger_service_1_1;
            }],
        execute: function() {
            /**
             * This Service get the server configuration to display in the login component
             * and execute the login and forgot password routines
             */
            LoginService = (function () {
                function LoginService(router, coreWebService, dotcmsEventsService, loggerService) {
                    var _this = this;
                    this.router = router;
                    this.coreWebService = coreWebService;
                    this.dotcmsEventsService = dotcmsEventsService;
                    this.loggerService = loggerService;
                    this._auth$ = new Subject_1.Subject();
                    this._logout$ = new Subject_1.Subject();
                    this.country = '';
                    this.lang = '';
                    this._loginAsUsersList$ = new Subject_1.Subject();
                    this.loginAsUserList = [];
                    this.urls = {
                        changePassword: 'v1/changePassword',
                        getAuth: 'v1/authentication/logInUser',
                        loginAs: 'v1/users/loginas/userid',
                        loginAsUserList: 'v1/users/loginAsData',
                        logout: 'v1/logout',
                        logoutAs: 'v1/users/logoutas',
                        recoverPassword: 'v1/forgotpassword',
                        serverInfo: 'v1/loginform',
                        userAuth: 'v1/authentication'
                    };
                    coreWebService.subscribeTo(401).subscribe(function () { return _this.logOutUser().subscribe(function () { }); });
                    // when the session is expired/destroyed
                    dotcmsEventsService.subscribeTo('SESSION_DESTROYED').pluck('data').subscribe(function (date) {
                        _this.loggerService.debug('Processing session destroyed: ', date);
                        _this.loggerService.debug('User Logged In Date: ', _this.auth.user.loggedInDate);
                        // if the destroyed event happens after the logged in date, so proceed!
                        if (_this.isLogoutAfterLastLogin(date)) {
                            _this.logOutUser().subscribe(function () { });
                        }
                    });
                }
                LoginService.prototype.isLogoutAfterLastLogin = function (date) {
                    return this.auth.user && this.auth.user.loggedInDate && date && Number(date) > Number(this.auth.user.loggedInDate);
                };
                Object.defineProperty(LoginService.prototype, "loginAsUsersList$", {
                    get: function () {
                        return this._loginAsUsersList$.asObservable();
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(LoginService.prototype, "auth$", {
                    get: function () {
                        return this._auth$.asObservable();
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(LoginService.prototype, "logout$", {
                    get: function () {
                        return this._logout$.asObservable();
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(LoginService.prototype, "auth", {
                    get: function () {
                        return this._auth;
                    },
                    enumerable: true,
                    configurable: true
                });
                Object.defineProperty(LoginService.prototype, "isLogin$", {
                    get: function () {
                        var _this = this;
                        return Rx_1.Observable.create(function (obs) {
                            if (_this.auth && _this.auth.user) {
                                obs.next(true);
                            }
                            else {
                                _this.loadAuth().subscribe(function (auth) { return obs.next(auth.user); });
                            }
                        });
                    },
                    enumerable: true,
                    configurable: true
                });
                /**
                 * Load _auth information.
                 * @returns {Observable<any>}
                 */
                LoginService.prototype.loadAuth = function () {
                    var _this = this;
                    return this.coreWebService.requestView({
                        method: http_1.RequestMethod.Get,
                        url: this.urls.getAuth
                    }).pluck('entity').map(function (auth) {
                        if (auth.user) {
                            _this.setAuth(auth);
                        }
                        return auth;
                    });
                };
                /**
                 * Change password
                 * @param password
                 * @param token
                 * @returns {Observable<any>}
                 */
                // TODO: is a common practice in JavaScript that when there is more than 2 params change it to a Object:
                // params.login, params.password and params.token in this case.
                LoginService.prototype.changePassword = function (password, token) {
                    var body = JSON.stringify({ 'password': password, 'token': token });
                    return this.coreWebService.requestView({
                        body: body,
                        method: http_1.RequestMethod.Post,
                        url: this.urls.changePassword,
                    });
                };
                /**
                 * Get specific user from the Login as user list
                 * @param id
                 * @returns {User}
                 */
                LoginService.prototype.getLoginAsUser = function (id) {
                    return this.loginAsUserList.filter(function (user) { return user.userId === id; })[0];
                };
                ;
                /**
                 * Get login as user list
                 * @returns {Observable<User[]>}
                 */
                LoginService.prototype.getLoginAsUsersList = function () {
                    var _this = this;
                    return Rx_1.Observable.create(function (observer) {
                        _this.loadLoginAsUsersList();
                        var loginAsUsersListSub = _this._loginAsUsersList$.subscribe(function (res) {
                            observer.next(res);
                            loginAsUsersListSub.unsubscribe();
                        });
                    });
                };
                /**
                 * Get the server information to configure the login component
                 * @param language language and country to get the internationalized messages
                 * @param i18nKeys array of message key to internationalize
                 * @returns {Observable<any>} Observable with an array of internationalized messages and server configuration info
                 */
                LoginService.prototype.getLoginFormInfo = function (language, i18nKeys) {
                    this.setLanguage(language);
                    return this.coreWebService.requestView({
                        body: { 'messagesKey': i18nKeys, 'language': this.lang, 'country': this.country },
                        method: http_1.RequestMethod.Post,
                        url: this.urls.serverInfo,
                    });
                };
                /**
                 * Request and store the login as _auth list.
                 */
                LoginService.prototype.loadLoginAsUsersList = function () {
                    var _this = this;
                    this.coreWebService.requestView({
                        method: http_1.RequestMethod.Get,
                        url: this.urls.loginAsUserList
                    }).pluck('entity', 'users').subscribe(function (data) {
                        _this.loginAsUserList = data;
                        _this._loginAsUsersList$.next(_this.loginAsUserList);
                    });
                };
                /**
                 * Get a user from the login as user list
                 * @param userId
                 * @returns {User[]}
                 */
                LoginService.prototype.getLoginAsUser = function (userId) {
                    return this.loginAsUserList.filter(function (item) { return item.userId === userId; })[0];
                };
                /**
                 * Do the login as request and return an Observable.
                 * @param options
                 * @returns {Observable<R>}
                 */
                // TODO: password in the url is a no-no, fix asap. Sanchez and Jose have an idea.
                LoginService.prototype.loginAs = function (options) {
                    var _this = this;
                    return this.coreWebService.requestView({
                        method: http_1.RequestMethod.Put,
                        url: this.urls.loginAs + "/" + options.userId + (options.password ? "/pwd/" + options.password : '')
                    }).map(function (res) {
                        if (!res.entity.loginAs) {
                            throw res.errorsMessages;
                        }
                        var loginAsUser = _this.getLoginAsUser(options.userId);
                        _this.setAuth({
                            loginAsUser: loginAsUser,
                            user: _this._auth.user
                        });
                        return res;
                    }).pluck('entity', 'loginAs');
                };
                /**
                 * Executes the call to the login rest api
                 * @param login User email or user id
                 * @param password User password
                 * @param rememberMe boolean indicating if the _auth want to use or not the remenber me option
                 * @param language string with the language and country code, ex: en_US
                 * @returns an array with the user if the user logged in successfully or the error message
                 */
                LoginService.prototype.loginUser = function (login, password, rememberMe, language) {
                    var _this = this;
                    this.setLanguage(language);
                    return this.coreWebService.requestView({
                        body: { 'userId': login, 'password': password, 'rememberMe': rememberMe, 'language': this.lang, 'country': this.country },
                        method: http_1.RequestMethod.Post,
                        url: this.urls.userAuth,
                    }).map(function (response) {
                        var auth = {
                            loginAsUser: null,
                            user: response.entity
                        };
                        _this.setAuth(auth);
                        return response.entity;
                    });
                };
                /**
                 * Logout "login as" user
                 * @returns {Observable<R>}
                 */
                LoginService.prototype.logoutAs = function () {
                    var _this = this;
                    return this.coreWebService.requestView({
                        method: http_1.RequestMethod.Put,
                        url: "" + this.urls.logoutAs
                    }).map(function (res) {
                        _this.setAuth({
                            loginAsUser: null,
                            user: _this._auth.user
                        });
                        return res;
                    });
                };
                /**
                 * Call the logout rest api
                 * @returns {Observable<any>}
                 */
                LoginService.prototype.logOutUser = function () {
                    var _this = this;   
                    /*var nullAuth = {
                            loginAsUser: null,
                            user: null
                    };  
                    _this.setAuth(nullAuth);*/
                    // on logout close the websocket
                    _this.dotcmsEventsService.destroy();
                    location.href = "/dotsaml/request/logout";
                };
                /**
                 * Executes the call to the recover passwrod rest api
                 * @param email User email address
                 * @returns an array with message indicating if the recover password was successfull
                 * or if there is an error
                 */
                LoginService.prototype.recoverPassword = function (login) {
                    var body = JSON.stringify({ 'userId': login });
                    return this.coreWebService.requestView({
                        body: { 'userId': login },
                        method: http_1.RequestMethod.Post,
                        url: this.urls.recoverPassword,
                    });
                };
                /**
                 * Subscribe to ser change and call received function on change.
                 * @param func function will call when user change
                 */
                LoginService.prototype.watchUser = function (func) {
                    if (this.auth) {
                        func(this.auth);
                    }
                    this.auth$.subscribe(function (auth) {
                        if (auth.user) {
                            func(auth);
                        }
                    });
                };
                /**
                 * Set logged_auth and update auth Observable
                 * @param _auth
                 */
                LoginService.prototype.setAuth = function (auth) {
                    this._auth = auth;
                    this._auth$.next(auth);
                    // When not logged user we need to fire the observable chain
                    if (!auth.user) {
                        this._logout$.next();
                    }
                    else {
                        this.dotcmsEventsService.start();
                    }
                };
                /**
                 * update the language and country variables from the string
                 * @param language string containing the language and country
                 */
                LoginService.prototype.setLanguage = function (language) {
                    if (language !== undefined && language !== '') {
                        var languageDesc = language.split('_');
                        this.lang = languageDesc[0];
                        this.country = languageDesc[1];
                    }
                    else {
                        this.lang = '';
                        this.country = '';
                    }
                };
                LoginService = __decorate([
                    core_1.Injectable(), 
                    __metadata('design:paramtypes', [router_1.Router, core_web_service_1.CoreWebService, dotcms_events_service_1.DotcmsEventsService, logger_service_1.LoggerService])
                ], LoginService);
                return LoginService;
            }());
            exports_1("LoginService", LoginService);
        }
    }
});
//# sourceMappingURL=login-service.js.map