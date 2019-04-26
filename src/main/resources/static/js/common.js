'use strict';

var $window = $(window);
var _alert_time;
var _confirm_time;
var _scroll_time;
var _switch_time;
var _reg_bank = new RegExp('^[0-9]{16,21}$');
var _reg_color = new RegExp('^#[0-9a-f]{3}$|^#[0-9a-f]{6}$|^rgba\\((\\s*(2[0-4][0-9]|25[0-5]|[01]?[0-9][0-9]?)\\s*,\\s*){3}\\s*(0\.[0-9]+|1|0)\\s*\\)$', 'i');
var _reg_car = new RegExp('^(([\u4E00-\u9FA5][a-zA-Z]|[\u4E00-\u9FA5]{2}d{2}|[\u4E00-\u9FA5]{2}[a-zA-Z])[-]?|([wW][Jj][\u4E00-\u9FA5]{1}[-]?)|([a-zA-Z]{2}))([A-Za-z0-9]{5}|[DdFf][A-HJ-NP-Za-hj-np-z0-9][0-9]{4}|[0-9]{5}[DdFf])$');
var _config = {
  website_name: '车V互助', //网站名称
  share_img: 'http://liuzhen674.w3.luyouxia.net/img/app_logo_1.jpg', //分享缩略图
  list_cache_time: 600000, //列表缓存时间（单位：毫秒）
  is_storage_upport: isStorageSupport(), //是否支持本地缓存
  is_app: false, //app环境
  is_wx: false, //微信环境
  is_wx_mobile: false, //移动端微信环境
  wx_config: false, //微信jssdk是否成功配置
  wx_ver: [0], //微信版本
  wx_share: ['updateAppMessageShareData', 'updateTimelineShareData', 'onMenuShareTimeline', 'onMenuShareAppMessage', 'onMenuShareQQ', 'onMenuShareWeibo', 'onMenuShareQZone'], //微信分享接口列表
  lipei_max: 1000 //理赔最高额度
};

// 默认分享文案
var _share_title = '车V互助|私家车小擦小挂互助平台';
var _share_desc = '9元起加入爱车互助社群，立享每年1000元维修互助金，85%车主一致的选择！';
var _share_link = 'http://liuzhen674.w3.luyouxia.net/view/index.html';

// 接口列表
var _api = {
  code: '/msm/verificationCode', // 获取验证码
  login: '/login', // 登录绑定
  user: '/loginUser', // 获取已登录用户信息
  search: '/search', // 搜索
  get_oauth: '/authorize/weixinAuth', // 获取微信授权地址
  city_list: '/city/list', // 获取城市列表
  role_list: '/role/list', // 获取已创建的角色列表
  role_detail: '/role/detail', // 角色详情
  role_type: '/role/type', // 获取角色类型列表
  role_add: '/role/save', // 创建角色
  role_edit: '/role/edit', // 编辑角色
  role_del: '/role/delete', // 删除角色
  car_invited: '/car/invited', // 查看邀请客户用的二维码
  car_list: '/car/list', // 车辆列表
  car_detail: '/car/detail', // 车辆详情
  car_edit: '/car/edit', // 编辑车辆
  insurance_list: '/car/list/insurance', // 保险公司列表
  agent_invited: '/agent/invited', // 查看邀请代理用的二维码
  agent_list: '/agent/list', // 代理人列表
  agent_detail: '/agent/detail', // 代理人详情
  agent_edit: '/agent/edit', // 编辑代理人
  channel_list: '/channel/list', // 渠道列表
  channel_detail: '/channel/detail', // 渠道详情
  channel_add: '/channel/save', // 创建渠道
  channel_edit: '/channel/edit', // 编辑渠道
  statistics_charge: '/statistical/amt/agent', // 统计充值金额
  statistics_refund: '/statistical/amt/refund', // 统计退款金额
  statistics_agent: '/statistical/count/agent', // 统计代理人数量
  statistics_car: '/statistical/count/car', // 统计车辆数量
  statistics_channel: '/statistical/count/channel', // 统计渠道数量
  statistics_car_page: '/statistical/car/page', // 统计用户列表
  statistics_car_pagg_filter: '/statistical/car/lab' // 统计用户列表（带筛选条件）
};

// 车辆状态
var _car_status = {
  '1': {
    name: '待支付',
    color: 'red'
  },
  '2': {
    name: '待添加照片',
    color: 'red'
  },
  '10': {
    name: '待审核',
    color: 'red'
  },
  '11': {
    name: '已通过',
    color: 'green'
  },
  '12': {
    name: '未通过',
    color: 'red'
  },
  '13': {
    name: '观察期',
    color: 'red'
  },
  '20': {
    name: '保障中',
    color: 'green'
  },
  '30': {
    name: '已退出计划',
    color: 'gray'
  },
  '31': {
    name: '不可用',
    color: 'gray'
  }
};

// 判断APP环境
if (window.navigator.userAgent.match(/chevdian|chevhuzhu/i)) {
  _config.is_app = true;
  $('body').addClass('app-bw');
}

// 判断微信环境和微信版本
if (window.navigator.userAgent.match(/micromessenger/i)) {
  _config.is_wx = true;
  if ($.os.tablet || $.os.phone) {
    _config.is_wx_mobile = true;
  }
  var r = window.navigator.userAgent.match(/micromessenger\/([1-9][0-9]*\.[0-9.]*)\s/i);
  if (r != null) {
    _config.wx_ver = r[1].split('.');
  }
} else if (!_config.is_app) {
  $('body').addClass('no-wx-bw');
}

// 注册juicer自定义函数
// 格式化车辆状态文本
var juicer_getCarStatus = function juicer_getCarStatus(status) {
  if (!status) return '';
  var st = _car_status[status];
  if (!st) return '';
  var str = st.name;
  if (!str) return '';
  return str;
};
juicer.register('build_carStatus', juicer_getCarStatus);
// 根据时间获取剩余天数
var juicer_getDays = function juicer_getDays(dt, days) {
  if ($.type(dt) === 'string' && /^\d+$/.test(dt) || $.type(dt) === 'number') {
    dt = new Date(dt);
  } else if ($.type(dt) !== 'date') {
    return '0';
  }
  if ($.type(days) !== 'number') {
    days = 0;
  } else {
    days = Math.floor(days);
  }
  dt = new Date(dt.getFullYear(), dt.getMonth(), dt.getDate() + days, dt.getHours(), dt.getMinutes(), dt.getSeconds());
  var dt_now = new Date();
  var dt_left = dt - dt_now;
  if (dt_left <= 0) return 0;
  return Math.ceil(dt_left / 86400000);
};
juicer.register('build_days', juicer_getDays);
// 根据时间获取已经过天数
var juicer_getLastDays = function juicer_getLastDays(dt, now_dt) {
  if ($.type(dt) === 'string' && /^\d+$/.test(dt) || $.type(dt) === 'number') {
    dt = new Date(dt);
  } else if ($.type(dt) !== 'date') {
    return '0';
  }
  if ($.type(now_dt) === 'string' && /^\d+$/.test(now_dt) || $.type(now_dt) === 'number') {
    now_dt = new Date(now_dt);
  } else if ($.type(now_dt) !== 'date') {
    now_dt = new Date();
  }
  var dt_left = now_dt - dt;
  if (dt_left <= 0) return 0;
  return Math.ceil(dt_left / 86400000);
};
juicer.register('build_lastDays', juicer_getLastDays);
// 格式化时间戳
var juicer_formatTime = function juicer_formatTime(timestamp, fstr) {
  if (!timestamp) return '';
  return new Date(timestamp).formatDate(fstr);
};
juicer.register('build_time', juicer_formatTime);
// 对url进行encode处理
var juicer_encodeUri = function juicer_encodeUri(str) {
  return encodeURIComponent(str);
};
juicer.register('build_encodeUri', juicer_encodeUri);
// Math.round
var juicer_round = function juicer_round(num) {
  if ($.type(num) !== 'number') return '0';
  return Math.round(num);
};
juicer.register('build_round', juicer_round);
// Math.ceil
var juicer_ceil = function juicer_ceil(num) {
  if ($.type(num) !== 'number') return '0';
  return Math.ceil(num);
};
juicer.register('build_ceil', juicer_ceil);
// Math.floor
var juicer_floor = function juicer_floor(num) {
  if ($.type(num) !== 'number') return '0';
  return Math.floor(num);
};
juicer.register('build_floor', juicer_floor);
// 格式化html代码
var juicer_formatHTML = function juicer_formatHTML(data, nowrap, len) {
  var str = data;
  if (len > 0) str = str.substr(0, len);
  if (!nowrap) str = str.replace(/\r\n|\r|\n/g, '<br>');
  return str.replace(/\\"/g, '"');
};
juicer.register('build_content', juicer_formatHTML);
// 替换文本
var juicer_replace = function juicer_replace(str, tar, sor) {
  if ($.type(str) !== 'string') {
    return '';
  } else {
    return str.replace(tar, sor);
  }
};
juicer.register('build_replace', juicer_replace);
// 截取文本
var juicer_formatDesc = function juicer_formatDesc(data, len) {
  var str = data.replace(/\\"/g, '"').replace(/<\/?.+?\/?>/g, '');
  if (len > 0) str = str.substr(0, len);
  return str;
};
juicer.register('build_desc', juicer_formatDesc);
// 格式化数字单位
var juicer_formatNum = function juicer_formatNum(data, ratio, unit) {
  var num = parseFloat(data);
  if ($.type(ratio) === 'number') num = num * ratio;
  if (num !== 0 && $.type(unit) === 'string') {
    var key = {
      '万': 10000,
      '千': 1000,
      '百': 100,
      'k': 1000,
      'K': 1000
    };
    if (key[unit] && key[unit] <= num) {
      num = Math.floor(num / key[unit] * 100) / 100 + unit;
    } else {
      num = Math.floor(num * 100) / 100;
    }
  } else {
    num = Math.floor(num * 100) / 100;
  }
  return num;
};
juicer.register('build_number', juicer_formatNum);

// API调用
function goAPI(dt) {
  if (!dt.url) return;
  var options = $.extend(true, {
    url: null,
    type: 'post',
    traditional: false,
    data: null,
    success: null,
    error: null,
    complete: null,
    skip_code: {}, //是否跳过指定code的错误处理
    ignore_abort: false, //是否忽略abort错误
    retry: 0, //服务器返回错误后重试次数
    retry_cbk: null //重试回调
  }, dt);
  if ($.type(options.retry) !== 'number') options.retry = 0;
  return $.ajax({
    url: options.url,
    type: options.type,
    data: new RegExp('post', 'i').test(options.type) ? JSON.stringify(options.data) : options.data,
    dataType: 'json',
    contentType: 'application/json',
    traditional: options.traditional,
    headers: options.headers,
    success: function success(data) {
      var response = data;
      if (response && response.code == 200 && $.isFunction(options.success)) {
        options.success(response);
      } else if (response && response.code != 200) {
        if (response.code == 99991000) {
          // 需要微信授权
          goAPI({
            url: _api.get_oauth,
            data: {
              strJson: window.location.href
            },
            success: function success(data) {
              window.location.href = data.result;
            }
          });
          return;
        } else if (response.code == 99990106) {
          // 需要绑定手机号
          window.location.href = '/view/bind.html?redir=' + encodeURIComponent(window.location.href);
          return;
        }
        if (options.retry > 0) {
          options.retry--;
          var xhr_new = goAPI(options);
          if ($.isFunction(options.retry_cbk)) options.retry_cbk(xhr_new);
        } else if ($.isFunction(options.error)) {
          options.error(response.message.trim() || '未知错误', response.code, response);
        }
      }
    },
    error: function error(xhr, errorType, err) {
      if (errorType === 'abort') {
        if (!options.ignore_abort) {
          if (options.retry > 0) {
            options.retry--;
            var xhr_new = goAPI(options);
            if ($.isFunction(options.retry_cbk)) options.retry_cbk(xhr_new);
          } else if ($.isFunction(options.error)) {
            options.error('网络连接不可用，请检查网络', -1, {});
          }
        }
      } else {
        if (options.retry > 0) {
          options.retry--;
          var xhr_new = goAPI(options);
          if ($.isFunction(options.retry_cbk)) options.retry_cbk(xhr_new);
        } else if ($.isFunction(options.error)) {
          options.error('服务器繁忙', xhr.status, {});
        }
      }
    },
    complete: function complete(xhr, status) {
      if ($.isFunction(options.complete)) options.complete();
    }
  });
}

// 检查是否需要微信授权
function wxOauth(cbk) {
  goAPI({
    url: _api.wx_oauth,
    type: 'GET',
    success: cbk,
    error: function error(data) {
      showConfirm({
        str: data,
        btn_yes: {
          str: '确定'
        }
      });
    }
  });
}

// 判断用户是否已关注公众号
function checkFollow(opt) {
  if (!opt) {
    return false;
  }
  goAPI({
    url: _api.check_follow,
    success: function success(data) {
      if ($.isFunction(opt.success)) {
        opt.success(data);
      }
    },
    error: function error(err) {
      if ($.isFunction(opt.fail)) {
        opt.fail(err);
      }
    },
    complete: function complete() {
      if ($.isFunction(opt.complete)) {
        opt.complete();
      }
    }
  });
}

// 验证用户是否已绑定手机号
function checkBind(cbk, err) {
  goAPI({
    url: _api.user,
    type: 'GET',
    success: function success(data) {
      if ($.isFunction(cbk)) {
        cbk(data.result);
      }
    },
    error: function error(msg, code) {
      if ($.isFunction(err)) {
        err(msg, code);
      }
    }
  });
}

// 设置错误页面信息
function setErrorPage(obj, msg, code, cbk) {
  if (code === -1) {
    obj.showRefresh({
      str: '网络开小差了，请检查手机网络\n或者尝试点击刷新',
      fixed: true,
      btn: {
        str: '刷新',
        event_click: cbk
      }
    });
  } else if (code > 500 && msg) {
    obj.showRefresh({
      str: msg + '（ ' + code + '）',
      fixed: true
    });
  } else {
    obj.showRefresh({
      str: '页面出现' + code + '错误，请联系客服',
      fixed: true
    });
  }
}

// 设定默认图像
function setDefaultImg(obj, img) {
  if (img) {
    $(obj).attr('src', img);
  }
}

// 检查微信授权
function checkAuthWX(dt) {
  goAPI({
    url: '/wx/checkauthorize',
    retry: 2,
    success: function success() {
      if ($.isFunction(dt.success)) dt.success();
    }
  });
}

// 配置微信JSSDK信息
function configWXJSSDK(apilist, opts) {
  var options = $.extend({ success: null, error: null, complete: null, debug: false }, opts);
  var config_status = true;
  goAPI({
    url: _api.wxjssdk,
    data: {
      url: encodeURIComponent(window.location.href.replace(/#.*$/, ''))
    },
    retry: 2,
    success: function success(data) {
      var data = data.data;
      wx.config({
        debug: options.debug,
        appId: data.appId,
        timestamp: data.timestamp,
        nonceStr: data.nonceStr,
        signature: data.signature,
        jsApiList: apilist
      });
      wx.ready(function () {
        if (config_status) _config.wx_config = true;
        if ($.isFunction(options.success)) options.success();
      });
      wx.error(function (res) {
        config_status = false;
        if ($.isFunction(options.error)) options.error(res.errMsg);
      });
    },
    error: options.error,
    complete: options.complete
  });
}

// 获取微信支付参数并调用微信支付
function doWXPay(opt, cbk, fail) {
  if (!opt) {
    return false;
  }
  showLoad();
  goAPI({
    url: _api.wxpay,
    data: {
      orderNo: opt.orderNo,
      associatorId: opt.associatorId
    },
    success: function success(data) {
      wx.chooseWXPay({
        appId: data.data.appId,
        timestamp: data.data.timeStamp.toString(),
        nonceStr: data.data.nonceStr,
        package: data.data.package,
        signType: data.data.signType,
        paySign: data.data.paySign,
        success: function success(res) {
          if ($.isFunction(cbk)) {
            cbk();
          }
        }
      });
    },
    error: function error(err) {
      if ($.isFunction(fail)) {
        fail(err);
      }
    },
    complete: function complete() {
      hideLoad();
    }
  });
}

// 微信客户端分享通用函数
function shareWX(data, _success, _cancel) {
  wx.updateAppMessageShareData({
    title: data.title,
    desc: data.desc,
    link: data.link,
    imgUrl: data.imgUrl ? data.imgUrl : _config.share_img,
    success: function success() {
      if ($.isFunction(_success)) _success();
    },
    cancel: function cancel() {
      if ($.isFunction(_cancel)) _cancel();
    }
  });
  wx.updateTimelineShareData({
    title: data.title,
    link: data.link,
    imgUrl: data.imgUrl ? data.imgUrl : _config.share_img,
    success: function success() {
      if ($.isFunction(_success)) _success();
    },
    cancel: function cancel() {
      if ($.isFunction(_cancel)) _cancel();
    }
  });
  wx.onMenuShareWeibo({
    title: data.title,
    desc: data.desc,
    link: data.link,
    imgUrl: data.imgUrl ? data.imgUrl : _config.share_img,
    success: function success() {
      if ($.isFunction(_success)) _success();
    },
    cancel: function cancel() {
      if ($.isFunction(_cancel)) _cancel();
    }
  });
  wx.onMenuShareQZone({
    title: data.title,
    desc: data.desc,
    link: data.link,
    imgUrl: data.imgUrl ? data.imgUrl : _config.share_img,
    success: function success() {
      if ($.isFunction(_success)) _success();
    },
    cancel: function cancel() {
      if ($.isFunction(_cancel)) _cancel();
    }
  });
  wx.onMenuShareTimeline({
    title: data.title,
    link: data.link,
    imgUrl: data.imgUrl ? data.imgUrl : _config.share_img,
    success: function success() {
      if ($.isFunction(_success)) _success();
    },
    cancel: function cancel() {
      if ($.isFunction(_cancel)) _cancel();
    }
  });
  wx.onMenuShareAppMessage({
    title: data.title,
    desc: data.desc,
    link: data.link,
    imgUrl: data.imgUrl ? data.imgUrl : _config.share_img,
    type: data.type ? data.type : 'link',
    dataUrl: data.type != 'link' ? data.dataUrl : null,
    success: function success() {
      if ($.isFunction(_success)) _success();
    },
    cancel: function cancel() {
      if ($.isFunction(_cancel)) _cancel();
    }
  });
  wx.onMenuShareQQ({
    title: data.title,
    desc: data.desc,
    link: data.link,
    imgUrl: data.imgUrl ? data.imgUrl : _config.share_img,
    success: function success() {
      if ($.isFunction(_success)) _success();
    },
    cancel: function cancel() {
      if ($.isFunction(_cancel)) _cancel();
    }
  });
}

// 微信上传图片接口
function wxUploadPhoto(data) {
  wx.uploadImage({
    localId: data.localId, // 需要上传的图片的本地ID，由chooseImage接口获得
    isShowProgressTips: data.isShowProgressTips ? data.isShowProgressTips : 1, // 默认为1，显示进度提示
    success: function success(res) {
      var serverId = res.serverId; // 返回图片的服务器端ID
      if ($.isFunction(data.success)) {
        data.success(res);
      }
    }
  });
}

// 获取URL参数
function getUrlPar(name, url) {
  var reg = new RegExp('(&|\\?)' + name + '=([^&]*)(&|$)', 'i');
  var src = url ? url.replace(/#.*$/, '') : window.location.search;
  var r = src.match(reg);
  if (r != null) {
    return r[2];
  } else {
    return '';
  }
}

// 是否支持本地缓存
function isStorageSupport() {
  try {
    window.sessionStorage.setItem('test', 'testValue');
    window.sessionStorage.removeItem('test');
    return true;
  } catch (e) {
    return false;
  }
}

// 向缓存写入内容
function saveStorage(key, val, islocal) {
  if (!_config.is_storage_upport) {
    return false;
  }
  try {
    var data = JSON.stringify(val);
    if (islocal) {
      window.localStorage.setItem(key.toString(), data);
    } else {
      window.sessionStorage.setItem(key.toString(), data);
    }
    return true;
  } catch (e) {
    return false;
  }
}

// 从缓存读取内容
function loadStorage(key, islocal) {
  if (!_config.is_storage_upport) {
    return null;
  }
  var data = islocal ? window.localStorage.getItem(key.toString()) : window.sessionStorage.getItem(key.toString());
  if (data) {
    try {
      data = JSON.parse(data);
      return data;
    } catch (e) {
      return null;
    }
  } else {
    return null;
  }
}

// 从缓存删除内容
function delStorage(key, islocal) {
  if (!_config.is_storage_upport) {
    return;
  }
  if (islocal) {
    window.localStorage.removeItem(key);
  } else {
    window.sessionStorage.removeItem(key);
  }
}

// 写入cookie
function saveCookie(key, val, isLong) {
  var ck = key + '=' + encodeURIComponent(JSON.stringify(val));
  if (isLong) {
    var date = new Date();
    var expiresDays = 365;
    date.setTime(date.getTime() + expiresDays * 8640000);
    ck += ';expires=' + date.toGMTString();
  }
  document.cookie = ck;
  return true;
}

// 读取cookie
function loadCookie(key) {
  var reg = new RegExp('(^| )' + key + '=([^;]*)(;|$)');
  var cookie = document.cookie.match(reg);
  if ($.isArray(cookie)) {
    cookie = cookie[2];
  }
  try {
    cookie = JSON.parse(decodeURIComponent(cookie));
  } catch (e) {
    cookie = null;
  }
  return cookie;
}

// 删除cookie
function delCookie(key) {
  var date = new Date();
  date.setTime(date.getTime() - 10000);
  document.cookie = key + '=;expires=' + date.toGMTString();
  return true;
}

// 底部弹窗菜单
function pushMenu(data) {
  var options = $.extend(true, {
    title: null, //菜单标题
    class: null, //自定义class
    cover_close: false, //是否允许点击遮罩层关闭菜单
    close_str: null, //关闭按钮的文本（特殊关键词：cross，只显示一个X）
    top_show: false, //是否显示顶部的标题栏
    menu: null, //数据源
    data: null, //自定义数据
    node: null, //自定义元素
    zIndex: 1000, //自定义z-index
    btn_yes: {
      str: null, //确定按钮文本(为空时不显示，默认为空)
      color: null, //确定按钮文本颜色
      href: null, //确定按钮链接
      click_close: true, //点击按钮后是否自动关闭提示框
      event_click: null //确定按钮回调函数
    },
    btn_no: {
      str: null, //否定按钮文本(为空时不显示，默认为空)
      color: null, //否定按钮文本颜色
      href: null, //否定按钮链接
      click_close: true, //点击按钮后是否自动关闭提示框
      event_click: null //否定按钮回调函数
    },
    btn_cancel: {
      str: null, //取消按钮文本(为空时不显示，默认为空)
      color: null, //取消按钮文本颜色
      href: null, //取消按钮链接
      click_close: true, //点击按钮后是否自动关闭提示框
      event_click: null //取消按钮回调函数
    },
    event_click: null //点击回调函数
  }, data);
  var push_time;
  var closing = false;
  var dely = 300;
  var duration = 500;
  var tpl = '<div class="push-bg"></div><div class="push-layer"><div class="push-top"><div class="push-title"></div><a class="hover" id="class-cancel" href="javascript:;"></a></div><div class="push-box"></div></div>';
  var $push = $('<div class="push-menu"></div>').append(tpl);
  var $push_layer = $push.find('.push-layer');
  var $push_bg = $push.find('.push-bg');
  var $push_top = $push.find('.push-top');
  var $push_title = $push.find('.push-title');
  var $push_box = $push.find('.push-box');
  var $push_cancel = $push.find('.push-cancel');
  var $push_bottom = '';
  $('body').append($push);
  if (options.menu || options.node) {
    initData();
  }

  function initData() {
    $push.attr('class', 'push-menu');
    var $push_btn_yes = options.btn_yes.str ? $('<a class="push-btn-yes fsize-16 hover fbox-cc" href="javascript:"></a>').text(options.btn_yes.str) : null;
    var $push_btn_no = options.btn_no.str ? $('<a class="push-btn-no fsize-16 hover fbox-cc" href="javascript:"></a>').text(options.btn_no.str) : null;
    var $push_btn_cancel = options.btn_cancel.str ? $('<a class="push-btn-cancel fsize-16 hover fbox-cc" href="javascript:"></a>').text(options.btn_cancel.str) : null;
    $push_cancel.off('click').on('click', hide);
    if (options.btn_yes.href) $btn_yes.attr('href', options.btn_yes.href);
    if (options.btn_no.href) $btn_no.attr('href', options.btn_no.href);
    if (options.btn_cancel.href) $btn_cancel.attr('href', options.btn_cancel.href);
    if (_reg_color.test(options.btn_yes.color)) $btn_yes.css('color', options.btn_yes.color);
    if (_reg_color.test(options.btn_no.color)) $btn_no.css('color', options.btn_no.color);
    if (_reg_color.test(options.btn_cancel.color)) $btn_cancel.css('color', options.btn_cancel.color);
    if (options.close_str === 'cross') {
      $push_cancel.text('＋').addClass('cross').removeClass('hide');
    } else if (options.close_str !== null) {
      $push_cancel.text(options.close_str).removeClass('hide cross');
    } else {
      $push_cancel.addClass('hide');
    }
    if (options.cover_close) {
      $push_bg.off('click').on('click', hide);
    } else {
      $push_bg.off('click');
    }
    if (options.menu) {
      var data = options.menu;
      if ($.isArray(data)) {
        if (data.length > 0) {
          var nodes = [];
          for (var i = 0, j = data.length; i < j; i++) {
            if ($.type(data[i]) === 'string' || $.type(data[i]) === 'number') {
              nodes.push($('<a class="push-item hover" href="javascript:;"></a>').data('value', data[i]).text(data[i]));
            } else {
              nodes.push($('<a class="push-item hover" href="javascript:;"></a>').data('value', data[i].value).text(data[i].name));
            }
          }
          $push_box.html(nodes).removeClass('default');
        } else {
          $push.addClass('default');
        }
      } else if ($.type(data) === 'string' || $.type(data) === 'number') {
        $push_box.html($('<a class="push-item hover" href="javascript:;"></a>').data('value', data).text(data)).removeClass('default');
      }
      $push.removeClass('default');
    } else {
      $push.addClass('default');
    }
    if (options.node) {
      var $push_node = $push_layer.find('.push-node');
      if ($push_node.length > 0) {
        $push_node.html(options.node);
      } else if ($push_bottom) {
        $push_bottom.before($('<div class="push-node"></div>').append(options.node));
      } else {
        $push_layer.append($('<div class="push-node"></div>').append(options.node));
      }
    }
    if ($push_btn_yes || $push_btn_no || $push_btn_cancel) {
      if ($push_bottom) {
        $push_bottom.html('').append($push_btn_yes, $push_btn_no, $push_btn_cancel);
      } else {
        $push_bottom = $('<div class="push-bottom"></div>').append($push_btn_yes, $push_btn_no, $push_btn_cancel);
        $push_layer.append($push_bottom);
      }
      if ($push_btn_yes && $push_btn_no && $push_btn_cancel) {
        $push_bottom.addClass('full');
      } else {
        $push_bottom.removeClass('full');
      }
    } else if ($push_bottom) {
      $push_bottom.remove();
      $push_bottom = '';
    }
    if (options.title) {
      $push_title.text(options.title).removeClass('default');
    } else {
      $push.addClass('default');
    }
    if (options.top_show) {
      $push_top.css('display', 'block');
    } else {
      $push_top.css('display', 'none');
    }
    if (options.class) {
      $push.addClass(options.class);
    }
    if (options.zIndex) {
      $push.css('z-index', options.zIndex);
    }

    if ($push_btn_yes) {
      $push_btn_yes.off().on('click', function () {
        if (options.btn_yes.click_close) hide();
        if ($.isFunction(options.btn_yes.event_click)) options.btn_yes.event_click(options.data, options.node);
      });
    }

    if ($push_btn_no) {
      $push_btn_no.off().on('click', function () {
        if (options.btn_no.click_close) hide();
        if ($.isFunction(options.btn_no.event_click)) options.btn_no.event_click(options.data, options.node);
      });
    }

    if ($push_btn_cancel) {
      $push_btn_cancel.off().on('click', function () {
        if (options.btn_cancel.click_close) hide();
        if ($.isFunction(options.btn_cancel.event_click)) options.btn_cancel.event_click(options.data, options.node);
      });
    }

    if ($.isFunction(options.event_click)) {
      $push_box.off('click').on('click', '.push-item', function () {
        var $this = $(this);
        options.event_click({
          name: $this.text(),
          value: $this.data('value')
        });
        hide();
      });
    } else {
      $push_box.off('click');
    }
  }

  function show(data) {
    if (data) {
      $.extend(true, options, data);
      if (data.data) {
        options.data = data.data;
      } else {
        options.data = null;
      }
      initData();
    }
    push_time = new Date().getTime();
    $push.addClass('open');
    setTimeout(function () {
      $push_layer.addClass('open');
    }, dely);
  }

  function hide() {
    if (closing) return;
    closing = true;
    $push_layer.removeClass('open');
    setTimeout(function () {
      if (new Date().getTime() - push_time >= dely) {
        $push.addClass('close');
        setTimeout(function () {
          if (new Date().getTime() - push_time >= duration + dely) {
            $push.removeClass('open close');
          }
          closing = false;
        }, duration);
      } else {
        closing = false;
      }
    }, dely);
  }

  return {
    show: show,
    hide: hide
  };
}

// 显示载入提示
function showLoad(data) {
  var options = $.extend(true, {
    str: '请稍候', //提示框文本
    cover: true, //是否带遮罩层（提示框关闭前遮罩层会阻止用户进行其它操作）
    cover_background: null, //遮罩层背景颜色（只接受rgba或16进制编码颜色，下同）
    color: null, //提示框文本颜色
    data: null, //带入回调函数的自定义参数
    closeable: false, //是否允许点击关闭
    event_click: null //点击回调函数
  }, data);
  if ($.type(options.str) !== 'string' && $.type(options.str) !== 'number') options.str = '';
  var $node = $('body').find('#load-fixed');
  var $text = $('<div id="load-box"></div>').html(options.str.replace(/\r\n|\r|\n/g, '<br>'));
  if ($node.length > 0) {
    $node.removeClass().empty().append($text);
  } else {
    $node = $('<div id="load-fixed"></div>').append($text);
    $('body').append($node);
  }
  if (_reg_color.test(options.color)) $node.css('color', options.color);
  if (_reg_color.test(options.cover_background)) $node.css('background', options.cover_background);
  if (!options.cover) $node.addClass('nocover');
  $node.off().on('click', function () {
    if (options.closeable) hideLoad();
    if ($.isFunction(options.event_click)) {
      options.event_click(options.data);
    }
  }).css('display', 'block');
  setTimeout(function () {
    $node.addClass('open');
  }, 50);
}

// 隐藏载入提示
function hideLoad() {
  var $node = $('body').find('#load-fixed');
  if ($node.length > 0) {
    $node.removeClass('open').addClass('close');
    setTimeout(function () {
      $node.css('display', 'none');
    }, 300);
  }
}

// 显示载入提示（插入元素中）
$.fn.showLoad = function (str) {
  var key_str = '载入中...';
  if ($.type(str) === 'string' || $.type(str) === 'number') key_str = str.replace(/\r\n|\r|\n/g, '<br>');
  var $node = this.find('.load-box');
  if ($node.length > 0) {
    $node.html(key_str).show();
  } else {
    this.append($('<div class="load-box"></div>').html(key_str));
  }
  return this;
};

// 隐藏载入提示（插入元素中）
$.fn.hideLoad = function () {
  var $node = this.find('.load-box');
  if ($node.length > 0) {
    $node.hide();
  }
  return this;
};

// 显示刷新提示
$.fn.showRefresh = function (data) {
  var default_btn_str = '点击重新加载';
  var options = $.extend({
    str: '', //提示文本
    color: null, //提示文本颜色
    node: null, //自定义元素
    fixed: false, //是否浮动居中
    btn: {
      str: null, //按钮文本（为空时不显示按钮）
      color: null, //按钮文本颜色
      background: null, //按钮背景颜色
      event_click: null //按钮点击事件
    }
  }, data);
  if ($.type(options.str) === 'string' || $.type(str) === 'number') options.str = options.str.toString().replace(/\r\n|\r|\n/g, '<br>');
  var $node = this.find('.refresh-box');
  if ($node.length > 0) {
    $node.empty().append($('<p></p>').html(options.str)).css({ 'display': 'block', 'opacity': '1' });
  } else {
    $node = $('<div class="refresh-box"></div>').append($('<p></p>').html(options.str));
    this.append($node);
  }
  if (options.fixed) {
    $node.addClass('fixed');
  } else {
    $node.removeClass('fixed');
  }
  if (options.node) {
    $node.prepend($('<div class="refresh-node"></div>').append(options.node));
  }
  if (_reg_color.test(options.color)) $node.css('color', options.color);
  if (options.btn.str) {
    if ($.type(options.btn.str) !== 'string' && $.type(options.btn.str) !== 'number') options.btn.str = default_btn_str;
    var btn_css = {};
    if (_reg_color.test(options.btn.color)) btn_css.color = options.btn.color;
    if (_reg_color.test(options.btn.background)) btn_css.background = options.btn.background;
    var $btn = $('<a href="javascript:" class="btn-refresh hover"></a>').css(btn_css).html(options.btn.str).on('click', function () {
      if ($.isFunction(options.btn.event_click)) {
        options.btn.event_click();
      }
    });
    $node.append($btn);
  }
  return this;
};

// 隐藏刷新提示
$.fn.hideRefresh = function () {
  var $node = this.find('.refresh-box');
  if ($node.length > 0) {
    $node.hide();
  }
  return this;
};

// 弹窗提示
function showAlert(data) {
  var str = '';
  if ($.type(data) === 'string' || $.type(data) === 'number') str = data;
  var $alert = $('body').find('#alert-box');
  if ($alert.length <= 0) {
    $alert = $('<div id="alert-box"></div>');
    $('body').append($alert);
  }
  _alert_time = new Date().getTime();
  var duration = 1500;
  var dely = 600;
  $alert.removeClass().css({ 'display': 'block', 'left': '0', 'width': 'auto', 'webkit-transform': 'translateX(0) translateY(-50%)', 'transform': 'translateX(0) translateY(-50%)' }).html(str.toString().replace(/\r\n|\r|\n/g, '<br>'));
  var _width = Math.ceil($alert.width()) + 1;
  var _height = Math.ceil($alert.height()) + 1;
  $alert.css({ 'left': '50%', 'width': _width, 'webkit-transform': 'translateX(-50%) translateY(-50%)', 'transform': 'translateX(-50%) translateY(-50%)' }).addClass('open');
  if ($window.width() < 750) {
    $alert.css({ 'max-width': '90%' });
  }
  setTimeout(function () {
    if (new Date().getTime() - _alert_time >= duration) {
      $alert.removeClass('open').addClass('close');
      setTimeout(function () {
        if (new Date().getTime() - _alert_time >= duration + dely) {
          $alert.css('display', 'none');
        }
      }, dely);
    }
  }, duration);
}

// 带按钮的弹窗提示框
function showConfirm(data) {
  var default_str_yes = '确定';
  var default_str_no = '否';
  var default_str_cancel = '取消';
  var options = $.extend(true, {
    str: '', //提示框文本
    color: null, //提示框文本颜色（只接受rgba或16进制编码颜色，下同）
    title: '', //提示框标题
    title_color: null, //提示框文本杨色
    background: null, //提示框背景颜色
    full: false, //是否需要显示大量文本内容
    cover: true, //是否带遮罩层（提示框关闭前遮罩层会阻止用户进行其它操作）
    cover_close: false, //点击遮罩层是否可以关闭提示框
    cover_background: null, //遮罩层背景颜色
    node: null, //可插入自定义元素，显示在文本与按钮之间
    class: '', //自定义class
    data: null, //带入回调函数的自定义参数
    event_init: null, //控件初始化时回调函数
    event_close: null, //控件关闭时回调函数
    btn_close: false, //是否显示关闭按钮（默认不显示）
    btn_yes: {
      str: default_str_yes, //确定按钮文本(为空时不显示，默认不为空)
      color: null, //确定按钮文本颜色
      href: null, //确定按钮链接
      click_close: true, //点击按钮后是否自动关闭提示框
      event_click: null //确定按钮回调函数
    },
    btn_no: {
      str: null, //否定按钮文本(为空时不显示，默认为空)
      color: null, //否定按钮文本颜色
      href: null, //否定按钮链接
      click_close: true, //点击按钮后是否自动关闭提示框
      event_click: null //否定按钮回调函数
    },
    btn_cancel: {
      str: null, //取消按钮文本(为空时不显示，默认为空)
      color: null, //取消按钮文本颜色
      href: null, //取消按钮链接
      click_close: true, //点击按钮后是否自动关闭提示框
      event_click: null //取消按钮回调函数
    }
  }, data);
  if ($.type(options.str) !== 'string' && $.type(options.str) !== 'number') options.str = '';
  if ($.type(options.title) !== 'string' && $.type(options.title) !== 'number') options.title = '';
  var $btn_yes = $('<a id="confirm-btn-yes" class="fbox-cc" href="javascript:"></a>').text(options.btn_yes.str);
  var $btn_no = $('<a id="confirm-btn-no" class="fbox-cc" href="javascript:"></a>').text(options.btn_no.str);
  var $btn_cancel = $('<a id="confirm-btn-cancel" class="fbox-cc" href="javascript:"></a>').text(options.btn_cancel.str);
  var $confirm_txt = $('<div id="confirm-txt"></div>').html(options.str.toString().replace(/\r\n|\r|\n/g, '<br>'));
  var $confirm_title = $('<div id="confirm-title"></div>').html(options.title.toString().replace(/\r\n|\r|\n/g, '<br>'));
  if (options.btn_yes.href) $btn_yes.attr('href', options.btn_yes.href);
  if (options.btn_no.href) $btn_no.attr('href', options.btn_no.href);
  if (options.btn_cancel.href) $btn_cancel.attr('href', options.btn_cancel.href);
  if (_reg_color.test(options.color)) $confirm_txt.css('color', options.color);
  if (_reg_color.test(options.title_color)) $confirm_title.css('color', options.title_color);
  if (_reg_color.test(options.btn_yes.color)) $btn_yes.css('color', options.btn_yes.color);
  if (_reg_color.test(options.btn_no.color)) $btn_no.css('color', options.btn_no.color);
  if (_reg_color.test(options.btn_cancel.color)) $btn_cancel.css('color', options.btn_cancel.color);
  var $confirm_box = $('<div id="confirm-box"></div>');
  if (options.str != '') {
    $confirm_box.prepend($confirm_txt);
  };
  if (options.title != '') {
    $confirm_box.prepend($confirm_title);
  };
  if (options.node) {
    $confirm_box.append($('<div id="confirm-node"></div>').append(options.node));
  };
  if (_reg_color.test(options.background)) {
    $confirm_box.css('background', options.background);
  }
  var $confirm_btn = $('<div id="confirm-btn"></div>').append(options.btn_cancel.str ? $btn_cancel : null, options.btn_no.str ? $btn_no : null, options.btn_yes.str ? $btn_yes : null);
  var $confirm_bg = $('<div id="confirm-bg"></div>');
  var $confirm_wrap = $('<div id="confirm-wrap"></div>').append($confirm_bg, $confirm_box.append($confirm_btn));
  var $confirm = $('body').find('#confirm-layer');
  if ($confirm.length > 0) {
    $confirm.removeClass().empty().append($confirm_wrap);
  } else {
    $confirm = $('<div id="confirm-layer"></div>').append($confirm_wrap);
    $('body').append($confirm);
  }
  if (options.cover && !options.full) {
    $confirm_wrap.on('touchmove', function (e) {
      e.preventDefault();
    });
  } else if (!options.cover) {
    $confirm.addClass('nocover');
  }
  if (_reg_color.test(options.cover_background)) {
    $confirm_bg.css('background', options.cover_background);
  }
  if (options.cover_close) {
    $confirm_bg.on('click', hideConfirm);
  }
  if (options.btn_close) {
    $confirm_box.prepend($('<a id="confirm-btn-close" class="hover" href="javascript:;"></a>').on('click', hideConfirm));
  }
  $confirm.css('display', 'block').addClass(options.class);
  if (options.full) {
    $confirm.addClass('full');
  } else {
    $confirm_box.css({ 'left': '0', 'width': 'auto', 'webkit-transform': 'translateX(0) translateY(-50%)', 'transform': 'translateX(0) translateY(-50%)' });
    var _width = Math.ceil($confirm_box.width() / $window.width() * 100);
    $confirm_box.css({ 'left': '50%', 'width': _width + '%', 'webkit-transform': 'translateX(-50%) translateY(-50%)', 'transform': 'translateX(-50%) translateY(-50%)' });
    if ($window.width() < 750) {
      $confirm_box.css({ 'max-width': '90%' });
    }
  }
  $confirm.addClass('open');
  if ($.isFunction(options.event_init)) options.event_init(options.data, options.node);
  _confirm_time = new Date().getTime();
  var dely = 400;
  if (options.btn_yes.str) {
    if ($.type(options.btn_yes.str) !== 'string' && $.type(options.btn_yes.str) !== 'number') $btn_yes.text(default_str_yes);
    $btn_yes.off().on('click', function () {
      if (options.btn_yes.click_close) hideConfirm();
      if ($.isFunction(options.btn_yes.event_click)) options.btn_yes.event_click(options.data, options.node);
    });
  }
  if (options.btn_no.str) {
    if ($.type(options.btn_no.str) !== 'string' && $.type(options.btn_no.str) !== 'number') $btn_no.text(default_str_no);
    $btn_no.off().on('click', function () {
      if (options.btn_no.click_close) hideConfirm();
      if ($.isFunction(options.btn_no.event_click)) options.btn_no.event_click(options.data, options.node);
    });
  }
  if (options.btn_cancel.str) {
    if ($.type(options.btn_cancel.str) !== 'string' && $.type(options.btn_cancel.str) !== 'number') $btn_cancel.text(default_str_cancel);
    $btn_cancel.off().on('click', function () {
      if (options.btn_cancel.click_close) hideConfirm();
      if ($.isFunction(options.btn_cancel.event_click)) options.btn_cancel.event_click(options.data, options.node);
    });
  }
  function hideConfirm() {
    $confirm.removeClass('open').addClass('close');
    setTimeout(function () {
      if (new Date().getTime() - _confirm_time >= dely) {
        $confirm.css('display', 'none');
        if ($.isFunction(options.event_close)) {
          options.event_close();
        }
      }
    }, dely);
  }
}

// 隐藏提示框
function hideConfirm() {
  var $confirm = $('body').find('#confirm-layer');
  if ($confirm.length > 0) {
    var dely = 400;
    $confirm.removeClass('open').addClass('close');
    setTimeout(function () {
      if (new Date().getTime() - _confirm_time >= dely) $confirm.css('display', 'none');
    }, dely);
  }
}

// 图片自适应居中
function resetImg(obj, type) {
  var img = $(obj);
  var target_w = '100%';
  var target_h = '100%';
  var target_l = 0;
  var target_t = 0;
  var img_w = img.width();
  var img_h = img.height();
  var con_w = img.parent().width();
  var con_h = img.parent().height();
  var scale_x = con_w / img_w;
  var scale_y = con_h / img_h;
  if (type === 'fix') {
    if (scale_x <= scale_y) {
      target_h = img_h * scale_x / con_h;
      target_t = (1 - target_h) / 2;
      target_h = Math.round(target_h * 10000) / 100 + '%';
      target_t = Math.round(target_t * 10000) / 100 + '%';
    } else {
      target_w = img_w * scale_y / con_w;
      target_l = (1 - target_w) / 2;
      target_w = Math.round(target_w * 10000) / 100 + '%';
      target_l = Math.round(target_l * 10000) / 100 + '%';
    }
  } else if (type === 'width') {
    target_h = img_h * scale_x / con_h;
    target_t = (1 - target_h) / 2;
    target_h = Math.round(target_h * 10000) / 100 + '%';
    target_t = Math.round(target_t * 10000) / 100 + '%';
  } else if (type === 'height') {
    target_w = img_w * scale_y / con_w;
    target_l = (1 - target_w) / 2;
    target_w = Math.round(target_w * 10000) / 100 + '%';
    target_l = Math.round(target_l * 10000) / 100 + '%';
  } else if (type === 'full') {
    target_w = '100%';
    target_h = '100%';
    target_l = '0';
    target_t = '0';
  } else if (scale_x >= scale_y) {
    target_h = img_h * scale_x / con_h;
    target_t = (1 - target_h) / 2;
    target_h = Math.round(target_h * 10000) / 100 + '%';
    target_t = Math.round(target_t * 10000) / 100 + '%';
  } else {
    target_w = img_w * scale_y / con_w;
    target_l = (1 - target_w) / 2;
    target_w = Math.round(target_w * 10000) / 100 + '%';
    target_l = Math.round(target_l * 10000) / 100 + '%';
  }
  img.css({
    'width': target_w,
    'height': target_h,
    'left': target_l,
    'top': target_t,
    'opacity': 1
  });
}

// 向元素中插入图片并自适应居中
$.fn.insertImg = function (src, type) {
  var $this = this;
  var img = new Image();
  img.onload = function () {
    img.onload = null;
    var target_w = '100%';
    var target_h = '100%';
    var target_l = 0;
    var target_t = 0;
    var img_w = img.width;
    var img_h = img.height;
    var con_w = $this.width();
    var con_h = $this.height();
    var scale_x = con_w / img_w;
    var scale_y = con_h / img_h;
    if (type === 'fix') {
      if (scale_x <= scale_y) {
        target_h = img_h * scale_x / con_h;
        target_t = (1 - target_h) / 2;
        target_h = Math.round(target_h * 10000) / 100 + '%';
        target_t = Math.round(target_t * 10000) / 100 + '%';
      } else {
        target_w = img_w * scale_y / con_w;
        target_l = (1 - target_w) / 2;
        target_w = Math.round(target_w * 10000) / 100 + '%';
        target_l = Math.round(target_l * 10000) / 100 + '%';
      }
    } else if (type === 'width') {
      target_h = img_h * scale_x / con_h;
      target_t = (1 - target_h) / 2;
      target_h = Math.round(target_h * 10000) / 100 + '%';
      target_t = Math.round(target_t * 10000) / 100 + '%';
    } else if (type === 'height') {
      target_w = img_w * scale_y / con_w;
      target_l = (1 - target_w) / 2;
      target_w = Math.round(target_w * 10000) / 100 + '%';
      target_l = Math.round(target_l * 10000) / 100 + '%';
    } else if (type === 'full') {
      target_w = '100%';
      target_h = '100%';
      target_l = '0';
      target_t = '0';
    } else if (scale_x >= scale_y) {
      target_h = img_h * scale_x / con_h;
      target_t = (1 - target_h) / 2;
      target_h = Math.round(target_h * 10000) / 100 + '%';
      target_t = Math.round(target_t * 10000) / 100 + '%';
    } else {
      target_w = img_w * scale_y / con_w;
      target_l = (1 - target_w) / 2;
      target_w = Math.round(target_w * 10000) / 100 + '%';
      target_l = Math.round(target_l * 10000) / 100 + '%';
    }
    img.style.width = target_w;
    img.style.height = target_h;
    img.style.left = target_l;
    img.style.top = target_t;
    img.style.opacity = '1';
  };
  img.src = src;
  return $this.append(img);
};

// 压缩图片
function resizeImg(data) {
  var options_default = {
    width: 800,
    quality: 0.8
  };
  var options = $.extend({}, options_default, data);
  lrz(data.file, { width: options.width, quality: options.quality }).then(function (rst) {
    if ($.isFunction(options.complete)) {
      options.complete();
    }
    if ($.isFunction(options.success)) {
      options.success(rst);
    }
    if ($.isFunction(options.always)) {
      options.always();
    }
    return rst;
  }).catch(function (err) {
    console.error(err);
    if ($.isFunction(options.complete)) {
      options.complete();
    }
    if ($.isFunction(options.fail)) {
      options.fail();
    }
    if ($.isFunction(options.always)) {
      options.always();
    }
  });
}

// 设置标题
function setTitle(str) {
  if ($.os.ios) {
    document.title = str;
    var $iframe = $('<iframe src="/favicon.ico"></iframe>');
    $iframe.on('load', function () {
      setTimeout(function () {
        $iframe.off('load').remove();
      }, 0);
    }).appendTo($('body'));
  } else {
    $('title').html(str);
  }
}

// 验证车牌号
function validateCar(str) {
  if (_reg_car.test(str)) return true;
  return false;
}

// 验证电话号码格式
function validatePhone(str, tel_only) {
  var reg = tel_only ? new RegExp('^1[3-9][0-9]{9}$') : new RegExp('^1[3-9][0-9]{9}$|^[0-9]{3}[\\s-]?[0-9]{8}$|^[0-9]{4}[\\s-]?[0-9]{7}$');
  if (reg.test(str)) return true;
  return false;
}

// 验证身份证号码格式
function validateID(str) {
  if (!str) return false;
  if (/^[1-9][0-9]{14}$/.test(str)) return true;
  if (/^[1-9][0-9]{16}[0-9xX]$/.test(str)) {
    var str = str.split('');
    var factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
    var parity = [1, 0, 'X', 9, 8, 7, 6, 5, 4, 3, 2];
    var sum = 0;
    var ai = 0;
    var wi = 0;
    for (var i = 0; i < 17; i++) {
      ai = str[i];
      wi = factor[i];
      sum = sum + ai * wi;
    }
    var last = parity[sum % 11];
    if (parity[sum % 11] == str[17].toUpperCase()) {
      return true;
    } else {
      return false;
    }
  } else {
    return false;
  }
}

// 格式化HTML代码
function formatHTML(str) {
  try {
    var rep = {
      '\r\n': '<br>',
      '\r': '<br>',
      '\n': '<br>',
      '\\"': '"',
      '+': ' '
    };
    return str.replace(/\r\n|\r|\n|\\"|\+/g, function (key) {
      var val = rep[key];
      if (val != null) {
        return val;
      } else {
        return '';
      }
    });
  } catch (e) {
    return str;
  }
}

// string转date
function toDate(str) {
  var dt = new Date(str);
  if (dt.toString() === 'Invalid Date') {
    if (/^[0-9]{8}$/.test(str)) {
      dt = new Date(str.substr(0, 4) + '-' + str.substr(4, 2) + '-' + str.substr(6));
    }
  }
  if (dt.toString() === 'Invalid Date') {
    return null;
  } else {
    return dt;
  }
}

// 自定义数字输入框初始化
$.fn.initNumberBox = function (data) {
  var options = $.extend({
    value: 0, //初始值
    max: 0, //最大值（小于1代表不限）
    input: true, //是否允许手动输入
    event_change: null //数据变动回调函数
  }, data);
  if ($.type(options.max) !== 'number') options.max = 0;
  return this.each(function () {
    var value = 0;
    var $input_box = $('<div class="num-typebox"></div>');
    var $input = $(this).on('blur', function () {
      if (value <= 0) {
        value = 1;
        $input.val(value);
        if ($.isFunction(options.event_change)) {
          options.event_change(value);
        }
      }
    });

    if (!options.input) {
      $input.prop('readonly', 'true');
    }

    if ($input[0].oninput === undefined) {
      $input[0].onpropertychange = valiNum; //针对不支持oninput事件的浏览器
    } else {
      $input.on('input', valiNum);
    }

    // 输入框数字验证
    function valiNum() {
      var val = $input.val().replace(/\s/g, '');
      if (val.length > 0) {
        try {
          var v = parseInt(val);
          if (v != value) {
            if (v > 0 && (options.max > 0 && v <= options.max || options.max <= 0)) {
              value = v;
            } else if (options.max && v > options.max) {
              value = options.max;
              $input.val(value);
            } else {
              value = 1;
              $input.val(value);
            }
            if ($.isFunction(options.event_change)) {
              options.event_change(value);
            }
          } else {
            $input.val(value);
          }
        } catch (e) {
          if (value != 1 && $.isFunction(options.event_change)) {
            options.event_change(value);
          }
          value = 1;
          $input.val(value);
        }
      } else {
        value = 0;
        if ($.isFunction(options.event_change)) {
          options.event_change(value);
        }
      }
    }

    // 输入框初始值
    if (options.value || $input.attr('value')) {
      try {
        var v = parseInt(options.value || $input.attr('value'));
        if (v > 0 && (options.max > 0 && v <= options.max || options.max <= 0)) {
          value = v;
        } else if (options.max && v > options.max) {
          value = options.max;
        } else {
          value = 1;
        }
        $input.val(value);
      } catch (e) {
        value = 1;
        $input.val(value);
      }
    } else {
      value = 1;
      $input.val(value);
    }

    // 减1
    var $btn_minus = $('<a class="num-minus hover" href="javascript:">-</a>').on('click', function () {
      if (value > 1) {
        value--;
        $input.val(value);
        if ($.isFunction(options.event_change)) {
          options.event_change(value);
        }
      }
    });

    // 加1
    var $btn_plus = $('<a class="num-plus hover" href="javascript:">+</a>').on('click', function () {
      if (options.max > 0 && value < options.max || options.max <= 0) {
        value++;
        $input.val(value);
        if ($.isFunction(options.event_change)) {
          options.event_change(value);
        }
      }
    });

    $input.after($input_box.append($btn_minus, $btn_plus)).appendTo($input_box);
  });
};

// 构建日期选择器所需数据源
function createDateSource(year_start, year_end) {
  var obj = {};
  var obj_28 = {};
  for (var i = 1; i < 29; i++) {
    obj_28[i] = {
      name: i + '日'
    };
  }
  var obj_29 = $.extend({}, obj_28, { '29': { name: '29日' } });
  var obj_30 = $.extend({}, obj_29, { '30': { name: '30日' } });
  var obj_31 = $.extend({}, obj_30, { '31': { name: '31日' } });
  if (year_end < year_start) {
    year_end = year_start;
  }
  while (year_end >= year_start) {
    var obj_year = {};
    for (var i = 1; i < 13; i++) {
      if (i === 1 || i === 3 || i === 5 || i === 7 || i === 8 || i === 10 || i === 12) {
        obj_year[i] = {
          name: i + '月',
          value: obj_31
        };
      } else if (i === 4 || i === 6 || i === 9 || i === 11) {
        obj_year[i] = {
          name: i + '月',
          value: obj_30
        };
      } else if (year_start % 400 === 0 || year_start % 100 !== 0 && year_start % 4 === 0) {
        obj_year[i] = {
          name: i + '月',
          value: obj_29
        };
      } else {
        obj_year[i] = {
          name: i + '月',
          value: obj_28
        };
      }
    }
    obj[year_start] = {
      name: year_start + '年',
      value: obj_year
    };
    year_start += 1;
  }
  return obj;
}

//日期扩展函数-格式化
Date.prototype.formatDate = function (format_str) {
  if ($.type(format_str) !== 'string') return this.toString();
  try {
    var year = this.getFullYear().toString();
    var month = this.getMonth() + 1;
    var month_full = month > 9 ? month : '0' + month;
    var day = this.getDate();
    var day_full = day > 9 ? day : '0' + day;
    var hour = this.getHours();
    var hour_full = hour > 9 ? hour : '0' + hour;
    var min = this.getMinutes();
    var min_full = min > 9 ? min : '0' + min;
    var sec = this.getSeconds();
    var sec_full = sec > 9 ? sec : '0' + sec;
    var rep = {
      'yyyy': year,
      'yyy': year.substr(1),
      'yy': year.substr(2),
      'y': year.substr(3),
      'MM': month_full,
      'M': month.toString(),
      'dd': day_full,
      'd': day.toString(),
      'hh': hour_full,
      'h': hour.toString(),
      'mm': min_full,
      'm': min,
      'ss': sec_full,
      's': sec,
      '年': '年',
      '月': '月',
      '日': '日',
      '时': '时',
      '分': '分',
      '秒': '秒',
      ':': ':',
      '：': '：',
      '-': '-',
      '－': '－',
      '.': '.',
      '\\': '\\',
      '/': '/',
      ' ': ' ',
      '　': '　'
    };
    var reg = new RegExp('y+|M+|d+|h+|m+|s+|年|月|日|时|分|秒-|\.|\\|/|.', 'g');
    if (reg.test(format_str)) {
      return format_str.replace(reg, function (key) {
        var val = rep[key];
        if (val != null) {
          return val;
        } else {
          return '';
        }
      });
    } else {
      return this.toString();
    }
  } catch (e) {
    return this.toString();
  }
};

// 数组扩展函数-元素插入数组
Array.prototype.insertAt = function (index, obj) {
  if (index < 0) index = 0;
  if (index > this.length) index = this.length;
  this.length++;
  for (var i = this.length - 1; i > index; i--) {
    this[i] = this[i - 1];
  }
  this[index] = obj;
};
// 数组扩展函数-数组追加数组
Array.prototype.append = function (arr) {
  if (!$.isArray(arr)) return;
  var index = this.length;
  for (var i = 0, j = arr.length; i < j; i++) {
    this[index] = arr[i];
    index++;
  }
};