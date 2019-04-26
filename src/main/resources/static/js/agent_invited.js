'use strict';

var tpl = juicer($('#main-tpl').html());

initPage();

// 初始化页面
function initPage() {
  showLoad();
  checkBind(function (user) {
    getInfo();
  }, function (msg, code) {
    hideLoad();
    setErrorPage($('.container'), msg, code, initPage);
  });
}

function getInfo() {
  showLoad();
  getQR(null, function (data) {
    var qr = data.agentQRcode || null;
    $('.container').html(tpl.render({
      qr: qr
    }));
    $('.container').html(tpl.render({
      qr: qr
    }));
  }, function (msg, code) {
    hideLoad();
    setErrorPage($('.container'), msg, code, getInfo);
  });
}

// 向服务器获取二维码
function getQR(dt, cbk, err) {
  goAPI({
    url: _api.agent_invited,
    type: 'get',
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

// 生成图片
function createImg() {
  setTimeout(function () {
    $('#main').height($('#main').height());
    $('#source').height($('#main').height());
    $('#qr img').removeAttr('onload');
    var dom = $('#main');
    var width = dom.width();
    var height = dom.height();
    var type = "png";
    var scaleBy = 1;
    var canvas = document.createElement('canvas');
    var rect = dom.get(0).getBoundingClientRect();
    canvas.width = width * scaleBy;
    canvas.height = height * scaleBy;
    canvas.style.width = width * scaleBy + 'px';
    canvas.style.height = height * scaleBy + 'px';
    var context = canvas.getContext('2d');
    context.scale(scaleBy, scaleBy);
    context.translate(-rect.left, -rect.top - $('#container').scrollTop());
    html2canvas(dom[0], {
      canvas: canvas,
      useCORS: true,
      onrendered: function onrendered(canvas) {
        $('#source').append($('<img>', { 'class': 'view', 'src': canvas.toDataURL('image/png') }));
        if ($('#source .view').length > 1) {
          $('#source .view:eq(0)').remove();
        }
        hideLoad();
      }
    });
  }, 300);
}