'use strict';
/* Controllers */
angular.module('restUI.controllers', [])
    .controller('AbnTestController', ['$scope', '$http',
        function($scope, $http) {
            $scope.resource_id = '';
            $scope.resource_url = 'rest/res/pretty/';
            $scope.method = 'GET';

            $scope.url = {
                resource_base: 'rest/res/',
                resource_pretty: 'rest/res/pretty/',
                resource_tree: 'rest/res/load/resourceTree',
                resource_definition: 'rest/res/file/',
                clean_resource: 'rest/res/clean/resource',
                reload_resource: 'rest/res/reload/'
            };

            //tree
            $scope.tree_data = [];
            $scope.resource_tree = {};

            $scope.tree_handler = function(branch) {

                if (branch.data.isLeaf) {
                    $http({
                        method: $scope.method,
                        url: $scope.url.resource_definition + branch.data.rid
                    }).
                    success(function(data) {
                        $scope.resource_editor_model = data;
                        $scope.resource_id = branch.data.rid;
                        //$scope.accordion_group_status.one.heading = 'Rest资源:' + $scope.resource_id;
                        $scope.add_alert({
                            type: 'success',
                            msg: '获取SQL Resource[' + $scope.resource_id + ']成功'
                        });
                        $scope.splice_query();
                    }).
                    error(function(data) {
                        $scope.add_alert({
                            type: 'danger',
                            msg: '获取SQL Resource[' + $scope.resource_id + ']失败'
                        });
                    });

                }
            };
            $scope.fetch_tree = function() {
                $http({
                    method: $scope.method,
                    url: $scope.url.resource_tree
                }).
                success(function(data) {
                    $scope.tree_data = data.treedata;
                }).
                error(function(data) {
                    $scope.tree_data = [];
                    $scope.add_alert({
                        type: 'danger',
                        msg: '获取SQL Resources列表失败'
                    });
                });
            };
            $scope.fetch_tree();

            $scope.clean_resource = function() {
                $http({
                    method: $scope.method,
                    url: $scope.url.clean_resource
                }).
                success(function(data) {
                    $scope.add_alert({
                        type: 'success',
                        msg: data
                    });
                }).
                error(function(data) {
                    $scope.add_alert({
                        type: 'danger',
                        msg: '清空SQL Resource缓存失败'
                    });
                });
            };

            $scope.refresh_and_clean = function() {
                $scope.fetch_tree();
                $scope.clean_resource();
            };

            $scope.reload_item = function() {
                $scope.clean_resource();
                if (!$scope.check_empty($scope.resource_id)) {

                    $http({
                        method: $scope.method,
                        url: $scope.url.resource_definition + $scope.resource_id
                    }).
                    success(function(data) {
                        $scope.resource_editor_model = data;
                        $scope.add_alert({
                            type: 'success',
                            msg: '重载SQL Resource[' + $scope.resource_id + ']成功'
                        });
                    }).
                    error(function(data) {
                        $scope.add_alert({
                            type: 'danger',
                            msg: '获取SQL Resource[' + $scope.resource_id + ']失败'
                        });
                    });
                }
            };

            //ace editor
            $scope.resource_editor_model = '';
            $scope.resourceOptions = {
                lineNumbers: true,
                theme: 'textmate',
                mode: 'xml'
            };

            $scope.aceeditor = {};
            $scope.resultOptions = {
                lineNumbers: true,
                // useWrapMode: true,
                theme: 'twilight',
                mode: 'json',
                onLoad: function(_ace) {
                    // _ace.getSession().setNewLineMode("auto");
                    // HACK to have the ace instance in the scope...
                    $scope.aceeditor = _ace;
                }
            };

            //accordion-group status
            $scope.accordion_group_status = {
                one: {
                    open: true,
                    heading: 'Res定义'
                },
                two: {
                    open: true,
                    heading: '数据查询'
                },
                three: {
                    open: false,
                    heading: '日志'
                }
            };

            //splice form
            $scope.radioOutputModel = 'json';
            $scope.splice = {
                filter: '',
                limit: '',
                offset: '',
                orderby: '',
                query: '',
                visible: '',
                output: 'json',
                link: '?'
            };
            $scope.splice_query = function() {

                if (!$scope.check_empty($scope.resource_id)) {
                    var val = $scope.resource_url + $scope.resource_id;
                    if (!$scope.check_empty($scope.splice.filter)) {
                        val = val + $scope.splice.link + '_filter=' + $scope.splice.filter;
                        $scope.splice.link = '&';
                    }
                    if (!$scope.check_empty($scope.splice.limit)) {
                        val = val + $scope.splice.link + '_limit=' + $scope.splice.limit;
                        $scope.splice.link = '&';
                    }
                    if (!$scope.check_empty($scope.splice.offset)) {
                        val = val + $scope.splice.link + '_offset=' + $scope.splice.offset;
                        $scope.splice.link = '&';
                    }
                    if (!$scope.check_empty($scope.splice.orderby)) {
                        val = val + $scope.splice.link + '_orderby=' + $scope.splice.orderby;
                        $scope.splice.link = '&';
                    }
                    if (!$scope.check_empty($scope.splice.visible)) {
                        val = val + $scope.splice.link + '_visible=' + $scope.splice.visible;
                        $scope.splice.link = '&';
                    }
                    val = val + $scope.splice.link + '_output=' + $scope.splice.output;

                    $scope.splice.link = '?';
                    $scope.splice.query = val;
                }
            };

            $scope.check_empty = function(str) {

                // return str === null || str === '' || str.match(/^ *$/) !== null;
                return str === null || str === '';
            };

            $scope.output_change = function(val) {
                $scope.splice.output = val;

                if (val == 'csv') {
                    $scope.aceeditor.getSession().setMode('ace/mode/' + 'sh');
                } else {
                    $scope.aceeditor.getSession().setMode('ace/mode/' + val);
                }

                $scope.splice_query();
            };

            $scope.radioFormatModel = 'format';
            $scope.formatModel = 'format';
            $scope.format_change = function(val) {
                $scope.formatModel = val;
                if (val === 'format') {
                    $scope.resource_url = $scope.url.resource_pretty;
                } else {
                    $scope.resource_url = $scope.url.resource_base;
                }

                $scope.splice_query();
            };

            $scope.query = function() {
                $scope.aceeditor.setValue('');
                var tmpQuery = $scope.splice.query;
                if (!$scope.check_empty($scope.splice.query)) {
                    $http({
                        method: $scope.method,
                        url: $scope.splice.query,
                        timeout: 30000
                    }).
                    success(function(data) {
                        if (angular.isObject(data) || angular.isArray(data)) {
                            if ($scope.formatModel === 'format') {
                                $scope.aceeditor.setValue(JSON.stringify(data, null, 2));
                            } else {
                                $scope.aceeditor.setValue(JSON.stringify(data));
                            }
                        } else {
                            $scope.aceeditor.setValue(data);
                        }
                    }).
                    error(function(data) {
                        $scope.add_alert({
                            type: 'danger',
                            msg: '超时(30秒)或获取SQL Resource[' + tmpQuery + ']查询结果失败'
                        });
                    });
                }
            };

            //alert
            $scope.alerts = [{
                type: 'success',
                msg: '欢迎登录Restful气象数据共享平台.'
            }];
            $scope.add_alert = function(val) {
                if ($scope.alerts.length >= 4) {
                    $scope.alerts.pop();
                }
                $scope.alerts.unshift(val);

            };

            $scope.closeAlert = function(index) {
                $scope.alerts.splice(index, 1);
            };


        }
    ]);
