'use strict';

(function () {
  var filter_channel = {};
  var filter_agent = {};
  var filter_channel_on = false; // 渠道筛选是否已打开
  var filter_agent_on = false; // 代理人筛选是否已打开

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
      $('.container').html(juicer($('#main-tpl').html(), user));
      loadData();
      // 点击渠道筛选
      $('#btn-filter-channel').on('click', function () {
        if (filter_channel_on) {
          filter_channel_on = false;
          $('#filter').removeClass('open');
          $('#btn-filter-channel').removeClass('active');
        } else {
          $('#filter-list').html('');
          filter_channel_on = true;
          filter_agent_on = false;
          $('#filter').addClass('open');
          $('#btn-filter-channel').addClass('active');
          $('#btn-filter-agent').removeClass('active');
          getFilter('10');
        }
      });

      // 点击代理人筛选
      $('#btn-filter-agent').on('click', function () {
        if (filter_agent_on) {
          filter_agent_on = false;
          $('#filter').removeClass('open');
          $('#btn-filter-agent').removeClass('active');
        } else {
          $('#filter-list').html('');
          filter_agent_on = true;
          filter_channel_on = false;
          $('#filter').addClass('open');
          $('#btn-filter-agent').addClass('active');
          $('#btn-filter-channel').removeClass('active');
          getFilter('20');
        }
      });

      // 点击清除筛选选项
      $('#btn-filter-clear').on('click', function () {
        filter_channel_on = false;
        filter_agent_on = false;
        filter_channel = {};
        filter_agent = {};
        $('#filter-channel').text('--');
        $('#filter-agent').text('--');
        $('#btn-filter-agent').removeClass('active');
        $('#btn-filter-channel').removeClass('active');
        $('#filter').removeClass('open');
        $('.btn-wrap').addClass('hide');
        loadData();
      });

      // 点击筛选选项
      $('#filter-list').on('click', '.f-item', function () {
        var item = $(this);
        item.toggleClass('active').siblings('.f-item').removeClass('active');
        if (item.hasClass('active')) {
          if (filter_channel_on) {
            filter_channel = {
              id: item.data('id'),
              name: item.data('name'),
              phone: item.data('phone')
            };
            filter_agent = {};
            $('#filter-channel').text(filter_channel.name);
            $('#filter-agent').text('--');
            if (filter_channel.phone) {
              $('.btn-wrap').removeClass('hide');
              $('#btn-phone').attr('href', 'tel:' + filter_channel.phone);
            } else {
              $('.btn-wrap').addClass('hide');
              $('#btn-phone').attr('href', 'javascript:;');
            }
          } else {
            filter_channel = {};
            filter_agent = {
              id: item.data('id'),
              name: item.data('name'),
              phone: item.data('phone')
            };
            $('#filter-agent').text(filter_agent.name);
            $('#filter-channel').text('--');
            if (filter_agent.phone) {
              $('.btn-wrap').removeClass('hide');
              $('#btn-phone').attr('href', 'tel:' + filter_agent.phone);
            } else {
              $('.btn-wrap').addClass('hide');
              $('#btn-phone').attr('href', 'javascript:;');
            }
          }
        } else {
          filter_channel = {};
          filter_agent = {};
          $('#filter-channel').text('--');
          $('#filter-agent').text('--');
          $('.btn-wrap').addClass('hide');
          $('#btn-phone').attr('href', 'javascript:;');
        }
        filter_channel_on = false;
        filter_agent_on = false;
        $('#btn-filter-agent').removeClass('active');
        $('#btn-filter-channel').removeClass('active');
        $('#filter').removeClass('open');
        loadData();
      });
    }, function (msg, code) {
      hideLoad();
      setErrorPage($('#main'), msg, code, initPage);
    });
  }

  // 加载数据
  function loadData() {
    var type = null;
    var id = null;
    if (filter_channel) {
      type = '10';
      id = filter_channel.id;
    } else if (filter_agent) {
      type = '20';
      id = filter_agent.id;
    }
    showLoad();
    getCarPage(type, id).done(function (list) {
      hideLoad();
      if (list.length === 0) {
        $('#main').html('').showRefresh({
          str: '未查询到客户',
          fixed: true
        });
      } else {
        $('#main').html(juicer($('#car-tpl').html(), list));
      }
    }).fail(function (err) {
      hideLoad();
      $('#main').html('');
      setErrorPage($('#main'), err.msg, err.code, loadData);
    });
  }

  // 获取筛选选项
  function getFilter(data) {
    showLoad();
    getChannelAgent(data).done(function (list) {
      hideLoad();
      if (list.length === 0) {
        $('#filter-list').html('').showRefresh({
          str: '未查询到内容',
          fixed: true
        });
      } else {
        $('#filter-list').html(juicer($('#filter-tpl').html(), {
          list: list,
          active_id: data == 10 ? filter_channel.id : filter_agent.id
        }));
      }
    }).fail(function (err) {
      hideLoad();
      $('#filter-list').html('');
      setErrorPage($('#filter-list'), err.msg, err.code, function () {
        getFilter(data);
      });
    });
  }

  // 向服务器获取代理人数量
  function getCarPage(type, id) {
    var dtd = $.Deferred();
    goAPI({
      url: type && id ? _api.statistics_car_pagg_filter + '/' + type + '/' + id : _api.statistics_car_page,
      data: {},
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

  // 向服务器获取渠道或者代理人列表
  function getChannelAgent(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.statistics_car_page + '/' + dt,
      data: {},
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
})();