'use strict';

(function () {
  var login_loading = false; //登录提交中
  var vali_loading = false; //验证码获取中
  var vali_colding = false; //验证码冷却中
  var uname_binded = false; //是否已绑定真实姓名
  var redirct_url = decodeURIComponent(getUrlPar('redir'));

  // if (!_config.is_wx) {
  // showConfirm({
  // str: '请在微信客户端中访问该页面'
  // })
  // return;
  // }

  // wxOauth();

  $('.container').html(juicer($('#main-tpl').html(), null));
  var $btn_code = $('#btn-code');

  $('#phone').on('blur', function () {
    var val = $(this).val();
    var err = $('.err-txt-phone');
    if (val.length > 0 && !validatePhone(val, true)) {
      err.text('手机号格式不正确');
    } else {
      err.text('');
    }
  });

  $('#code').on('blur', function () {
    var val = $(this).val();
    var err = $('.err-txt-code');
    if (val.length > 0 && !/^[0-9]{4}$/.test(val)) {
      err.text('验证码必须为4位数字');
    } else {
      err.text('');
    }
  });

  $('#uname').on('blur', function () {
    var val = $(this).val();
    var err = $('.err-txt-uname');
    if (val.length > 0) {
      err.text('');
    }
  });

  // 重新发送验证码
  $btn_code.on('click', function () {
    var phone = $('#phone').val();
    if (phone.length === 0) {
      $('.err-txt-phone').text('手机号码不能为空');
    } else if (!validatePhone(phone, true)) {
      $('.err-txt-phone').text('手机号码格式不正确');
    } else {
      sendValiCode(phone);
    }
  });

  // 发送验证码
  function sendValiCode(phone) {
    if (vali_loading || vali_colding) return;
    showLoad({ str: '验证码发送中' });
    vali_loading = true;
    goAPI({
      url: _api.code,
      type: 'get',
      data: {
        phoneNo: phone
      },
      success: function success(data) {
        showAlert('验证码已发送，请注意查收');
        coldValiCode(60);
        if (data.result == 1) {
          uname_binded = true;
        } else {
          $('#sub').removeClass('hide');
        }
      },
      error: function error(data) {
        $btn_code.text('发送验证码');
        showConfirm({
          str: data
        });
      },
      complete: function complete() {
        hideLoad();
        vali_loading = false;
      }
    });
  }

  // 验证码冷却
  function coldValiCode(timer) {
    if (timer > 0) {
      vali_colding = true;
      $btn_code.text(timer + 's');
      setTimeout(function () {
        coldValiCode(timer - 1);
      }, 1000);
    } else {
      vali_colding = false;
      $btn_code.text('发送验证码').removeClass('disabled');
    }
  }

  // 提交
  $('#btn-confirm').on('click', function () {
    if (login_loading) return;
    var phone = $('#phone').val().trim();
    var code = $('#code').val().trim();
    var uname = $('#uname').val().trim();
    var confirm = true;
    if (phone.length === 0) {
      $('.err-txt-phone').text('手机号码不能为空');
      confirm = false;
    } else if (!validatePhone(phone, true)) {
      $('.err-txt-phone').text('手机号码格式不正确');
      confirm = false;
    }
    if (code.length === 0) {
      $('.err-txt-code').text('验证码不能为空');
      confirm = false;
    } else if (!/^[0-9]{4}$/.test(code)) {
      $('.err-txt-code').text('验证码必须为4位数字');
      confirm = false;
    }
    if (!uname_binded && uname.length === 0) {
      $('.err-txt-uname').text('姓名不能为空');
      confirm = false;
    }
    if (confirm) {
      login({
        phone: phone,
        code: code,
        uname: uname
      });
    }
  });

  // 登录
  function login(data) {
    if (login_loading) return;
    login_loading = true;
    showLoad({
      str: '登录中'
    });
    goAPI({
      url: _api.login,
      data: {
        code: data.code,
        phoneNo: data.phone,
        userName: data.uname
      },
      success: function success(data) {
        if (new RegExp('^http://liuzhen674.w3.luyouxia.net', 'i').test(to_url)) {
          window.location.href = to_url;
        } else {
          window.location.href = '/view/index.html';
        }
      },
      error: function error(data) {
        showConfirm({
          str: data
        });
      },
      complete: function complete() {
        login_loading = false;
        hideLoad();
      }
    });
  }
})();