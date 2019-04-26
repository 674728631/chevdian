'use strict';

(function () {
  var profile_animation = false;
  var profile_show = false;
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
      hideLoad();
      $('.container').html(juicer($('#main-tpl').html(), user || {}));
      $('#btn-profile').on('click', showProfile);
      $('#profile-bg').on('click', hideProfile);
      $('.container').on('swipeRight', showProfile).on('swipeLeft', hideProfile).on('touchmove', function (e) {
        e.preventDefault();
      });
    }, function (msg, code) {
      hideLoad();
      setErrorPage($('.container'), msg, code, initPage);
    });
  }

  // 显示个人菜单
  function showProfile() {
    if (profile_animation || profile_show) {
      return;
    }
    profile_show = true;
    profile_animation = true;
    $('#profile-box').removeClass('hide');
    setTimeout(function () {
      $('#profile-content').addClass('open');
      setTimeout(function () {
        profile_animation = false;
      }, 500);
    }, 50);
  }

  // 隐藏个人菜单
  function hideProfile() {
    if (profile_animation || !profile_show) {
      return;
    }
    profile_show = false;
    profile_animation = true;
    $('#profile-content').removeClass('open');
    setTimeout(function () {
      $('#profile-box').addClass('hide');
      profile_animation = false;
    }, 500);
  }
})();