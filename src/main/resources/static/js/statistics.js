'use strict';

(function () {
  var user_data;
  var tpl_main = juicer($('#main-tpl').html());
  var province = {};
  var city = {};
  var date_start = null;
  var date_end = null;
  var datapick_date_start;
  var datapick_date_end;
  var datapick_area;

  // if (!_config.is_wx) {
  // showConfirm({
  // str: '请在微信客户端中访问该页面'
  // })
  // return;
  // }

  // wxOauth();

  initPage();

  // 初始化页面
  function initPage() {
    showLoad();
    checkBind(function (user) {
      user_data = user;
      loadData(user_data);
      // 点击显示日期选择弹窗
      $('#btn-filter-date').on('click', function () {
        showConfirm({
          node: '\n            <div id="date-box" class="fbox-ac">\n              <a id="btn-date-start" class="btn-date fbox-ac hover" href="javascript:;">\n                <span class="m-ico i5"></span>\n                <p id="date-start" class="ellipsis fbox-f1">\u5F00\u59CB\u65F6\u95F4</p>\n                <span class="arr2 arr-bottom"></span>\n              </a>\n              <p class="space">\u2014</p>\n              <a id="btn-date-end" class="btn-date fbox-ac hover" href="javascript:;">\n                <span class="m-ico i5"></span>\n                <p id="date-end" class="ellipsis fbox-f1">\u7ED3\u675F\u65F6\u95F4</p>\n                <span class="arr2 arr-bottom"></span>\n              </a>\n            </div>\n          ',
          event_init: function event_init() {
            if (date_start) {
              $('#date-start').text(date_start.formatDate('yyyy.MM.dd'));
            }
            if (date_end) {
              $('#date-end').text(date_end.formatDate('yyyy.MM.dd'));
            }
          },
          btn_yes: {
            str: '确定',
            click_close: false,
            event_click: function event_click() {
              if (date_start > date_end) {
                showAlert('开始时间不能大于结束时间');
                return;
              }
              $('#filter-date').text(date_start.formatDate('yyyy.MM.dd') + '-' + date_end.formatDate('yyyy.MM.dd'));
              loadData(user_data);
              hideConfirm();
            }
          },
          btn_cancel: {
            str: '取消'
          }
        });
        // 点击打开开始时间选择器
        $('#btn-date-start').off('click').on('click', function () {
          if (datapick_date_start) {
            datapick_date_start.show(date_start ? [date_start.formatDate('yyyy'), date_start.formatDate('M'), date_start.formatDate('d')] : null);
          }
        });
        // 点击打开结束时间选择器
        $('#btn-date-end').off('click').on('click', function () {
          if (datapick_date_end) {
            datapick_date_end.show(date_end ? [date_end.formatDate('yyyy'), date_end.formatDate('M'), date_end.formatDate('d')] : null);
          }
        });
      });

      // 点击打开地区选择器
      $('#btn-filter-area').on('click', function () {
        if (datapick_area) {
          datapick_area.show([province.value, city.value]);
        }
      });

      // 点击清除筛选选项
      $('#btn-filter-clear').on('click', function () {
        province = {};
        city = {};
        date_start = null;
        date_end = null;
        $('#filter-area').text('--');
        $('#filter-date').text('--');
        loadData(user_data);
      });
    }, function (msg, code) {
      hideLoad();
      setErrorPage($('#main'), msg, code, initPage);
    });
  }

  // 加载数据
  function loadData(user) {
    showLoad();
    var opt = {};
    if (date_start) {
      opt.beginTime = date_start.formatDate('yyyy-MM-dd');
    }
    if (date_end) {
      opt.endTime = date_end.formatDate('yyyy-MM-dd');
    }
    if (city) {
      opt.cityId = city.value;
    }
    if (user.userType == 30) {
      $.when(getCityList(), getChargeAmt(opt), getRefundAmt(opt), getCarCount(opt), getAgentCount(opt), getChannelCount(opt)).done(initData).fail(function (err) {
        hideLoad();
        $('#main').html('');
        setErrorPage($('#main'), err.msg, err.code, function () {
          loadData(user);
        });
      });
    } else if (user.userType == 10) {
      $.when(getCityList(), getChargeAmt(opt), getRefundAmt(opt), getCarCount(opt), getAgentCount(opt)).done(initData).fail(function (err) {
        hideLoad();
        $('#main').html('');
        setErrorPage($('#main'), err.msg, err.code, function () {
          loadData(user);
        });
      });
    } else {
      $.when(getCityList(), getChargeAmt(opt), getRefundAmt(opt), getCarCount(opt)).done(initData).fail(function (err) {
        hideLoad();
        $('#main').html('');
        setErrorPage($('#main'), err.msg, err.code, function () {
          loadData(user);
        });
      });
    }
  }

  // 填充数据
  function initData(cityList, chargeAmt, refundAmt, carCount, agentCount, channelCount) {
    hideLoad();
    if (!datapick_area) {
      datapick_area = datapicker2({
        data: initCityList(cityList),
        active_level: 2,
        event_ok: function event_ok(data) {
          province = data[0];
          city = data[1];
          $('#filter-area').text(province.name === city.name ? province.name : province.name + city.name);
          loadData(user_data);
        }
      });
    }
    if (!datapick_date_start) {
      datapick_date_start = datapicker2({
        data: createDateSource(2018, 2100),
        active_level: 3,
        event_ok: function event_ok(data) {
          date_start = new Date(data[0].value, data[1].value - 1, data[2].value, 0, 0, 0);
          $('#date-start').text(date_start.formatDate('yyyy.MM.dd'));
        }
      });
    }
    if (!datapick_date_end) {
      datapick_date_end = datapicker2({
        data: createDateSource(2018, 2100),
        active_level: 3,
        event_ok: function event_ok(data) {
          date_end = new Date(data[0].value, data[1].value - 1, data[2].value, 0, 0, 0);
          $('#date-end').text(date_end.formatDate('yyyy.MM.dd'));
        }
      });
    }
    $('#main').html(tpl_main.render({
      user: user_data,
      data: {
        chargeAmt: chargeAmt,
        refundAmt: refundAmt,
        carCount: carCount,
        agentCount: agentCount,
        channelCount: channelCount
      }
    }));
  }

  // 向服务器获取代理人数量
  function getAgentCount(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.statistics_agent,
      data: dt,
      success: function success(data) {
        dtd.resolve(data.result);
      },
      error: function error(msg, code) {
        dtd.resolve('?');
      }
    });
    return dtd.promise();
  }

  // 向服务器获取车辆数量
  function getCarCount(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.statistics_car,
      data: dt,
      success: function success(data) {
        dtd.resolve(data.result);
      },
      error: function error(msg, code) {
        dtd.resolve('?');
      }
    });
    return dtd.promise();
  }

  // 向服务器获取渠道数量
  function getChannelCount(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.statistics_channel,
      data: dt,
      success: function success(data) {
        dtd.resolve(data.result);
      },
      error: function error(msg, code) {
        dtd.resolve('?');
      }
    });
    return dtd.promise();
  }

  // 向服务器获取充值金额
  function getChargeAmt(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.statistics_charge,
      data: dt,
      success: function success(data) {
        dtd.resolve(data.result);
      },
      error: function error(msg, code) {
        dtd.resolve('?');
      }
    });
    return dtd.promise();
  }

  // 向服务器获取退款金额
  function getRefundAmt(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.statistics_refund,
      data: dt,
      success: function success(data) {
        dtd.resolve(data.result);
      },
      error: function error(msg, code) {
        dtd.resolve('?');
      }
    });
    return dtd.promise();
  }

  // 向服务器获取地区列表
  function getCityList() {
    var dtd = $.Deferred();
    goAPI({
      url: _api.city_list,
      type: 'get',
      success: function success(data) {
        dtd.resolve(data.result);
      },
      error: function error(msg, code) {
        dtd.reject({
          msg: msg,
          code: code
        });
      }
    });
    return dtd.promise();
  }

  // 构建城市选择器的数据源
  function initCityList(arr) {
    var obj = {};
    if ($.isArray(arr)) {
      arr.map(function (province) {
        var obj_province = {};
        if ($.isArray(province.cities)) {
          province.cities.map(function (city) {
            obj_province[city.id] = {
              name: city.cityName
            };
          });
        }
        obj[province.id] = {
          name: province.cityName,
          value: obj_province
        };
      });
    }
    return obj;
  }
})();