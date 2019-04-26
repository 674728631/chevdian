'use strict';

(function () {
  var loading = false; //接口请求中
  var list_loading = false; //列表请求中
  var list_page = 1; //列表加载页码
  var list_has_more = true; //列表是否还有下一页
  var list_tpl = juicer($('#list-tpl').html()); //列表模板
  var main_tpl = juicer($('#main-tpl').html()); //主模板
  var pm_role_detail = null; //角色详情的弹窗控件
  var pm_role_add = null; //创建角色的弹窗控件
  var pm_role_edit = null; //编辑角色的弹窗控件
  var role_type_selected = ''; //当前选中的角色类型ID
  var role_power = ''; //是否开启权限
  var datapick_type; //角色类型选择器控件
  var data_role_type = {}; //角色类型数据源 
  // juicer函数，根据角色类型ID获取对应的角色类型名称
  var juicer_getRoleName = function juicer_getRoleName(id) {
    return data_role_type[id].name || '未知角色';
  };
  juicer.register('juicer_getRoleName', juicer_getRoleName);

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
    list_loading = true;
    $.when(getRoleList(list_page), getRoleType()).done(function (role_list, role_type) {
      hideLoad();
      list_loading = false;

      // 构建角色类型选择器要求的数据源
      role_type.map(function (item) {
        if (item.id) {
          data_role_type[item.id] = {
            name: item.roleName || '未知角色'
          };
        }
      });

      // 初始化角色类型选择器
      datapick_type = datapicker2({
        data: data_role_type,
        event_ok: function event_ok(data) {
          role_type_selected = data[0].value;
          $('#add-input-type').val(data[0].name);
          $('.err-txt-role-type').text('');
        }
      });

      // 填充html模板
      $('.container').html(main_tpl.render(role_list || {}));

      // 更新列表相关参数
      list_has_more = role_list.hasNextPage;
      list_page += 1;

      // 点击显示“角色详情”的弹窗
      $('#list-box').on('click', '.permission-item', function () {
        var id = $(this).data('id');
        if (!id) {
          showAlert('未找到该角色的ID');
          return;
        }
        if (loading) {
          return;
        }
        loading = true;
        showLoad();
        getRoleDetail(id).done(function (role) {
          loading = false;
          hideLoad();
          initRoleDetail(role);
        }).fail(function (err) {
          loading = false;
          hideLoad();
          showAlert(err.msg);
        });
      });

      // 点击显示“创建角色”的弹窗
      $('#btn-add').on('click', function () {
        role_type_selected = '';
        role_power = '';
        if (!pm_role_add) {
          pm_role_add = pushMenu({
            class: 'add-menu',
            btn_yes: {
              str: '确定',
              click_close: false,
              event_click: addRole
            },
            btn_cancel: {
              str: '取消'
            }
          });
        }
        pm_role_add.show({
          node: juicer($('#add-tpl').html(), null)
        });
        // 选择权限
        $('.add-per').off('click').on('click', 'a', function () {
          $(this).toggleClass('active');
          if ($this.hasClass('active')) {
            role_power = 1;
          } else {
            role_power = '';
          }
        });
        // 点击显示角色类型选择器
        $('#add-input-type').off('click').on('click', function () {
          datapick_type.show(role_type_selected);
        });
        // 输入框输入检测
        $('#role-name').off('blur').on('blur', function () {
          var val = $(this).val();
          var err = $('.err-txt-role-name');
          if (val.length > 0) {
            err.text('');
          }
        });
      });

      // 滑动到底部加载下一页
      var dis = $window.scrollTop();
      $window.on('scroll', function () {
        if (list_has_more && !list_loading && dis <= $window.scrollTop() && $window.scrollTop() + $window.height() + 20 > $(document).height()) {
          loadRoleList();
        }
        dis = $window.scrollTop();
      });
    }).fail(function (err) {
      hideLoad();
      setErrorPage($('.container'), err.msg, err.code, initPage);
    });
  }

  // 初始化角色详情子页面
  function initRoleDetail(role) {
    if (!pm_role_detail) {
      pm_role_detail = pushMenu({
        class: 'add-menu',
        btn_yes: {
          str: '编辑',
          click_close: false,
          event_click: function event_click(role) {
            role_type_selected = role.roleId;
            role_power = role.power;
            if (!pm_role_edit) {
              pm_role_edit = pushMenu({
                class: 'add-menu',
                btn_yes: {
                  str: '确定',
                  click_close: false,
                  event_click: function event_click() {
                    addRole(role);
                  }
                },
                btn_no: {
                  str: '删除',
                  click_close: false,
                  event_click: function event_click() {
                    showConfirm({
                      str: '确定删除该角色吗？',
                      btn_yes: {
                        str: '确定',
                        event_click: function event_click() {
                          delRole(role);
                        }
                      },
                      btn_cancel: {
                        str: '取消'
                      }
                    });
                  }
                },
                btn_cancel: {
                  str: '取消'
                }
              });
            }
            pm_role_edit.show({
              node: juicer($('#add-tpl').html(), role)
            });
            // 选择权限
            $('.add-per').off('click').on('click', 'a', function () {
              $(this).toggleClass('active');
              if ($(this).hasClass('active')) {
                role_power = 1;
              } else {
                role_power = '';
              }
            });
            // 点击显示角色类型选择器
            $('#add-input-type').off('click').on('click', function () {
              datapick_type.show(role_type_selected);
            });
            // 输入框输入检测
            $('#role-name').off('blur').on('blur', function () {
              var val = $(this).val();
              var err = $('.err-txt-role-name');
              if (val.length > 0) {
                err.text('');
              }
            });
          }
        },
        btn_cancel: {
          str: '取消'
        }
      });
    }
    pm_role_detail.show({
      data: role,
      node: juicer($('#detail-tpl').html(), role)
    });
  }

  // 加载当前用户已创建的角色列表
  function loadRoleList() {
    list_loading = true;
    showLoad();
    getRoleList(list_page).done(function (data) {
      hideLoad();
      list_loading = false;
      list_has_more = data.hasNextPage;
      if (list_page === 1) {
        $('#list-box').html(list_tpl.render(data));
      } else {
        $('#list-box').append(list_tpl.render(data));
      }
      list_page += 1;
    }).fail(function (err) {
      hideLoad();
      list_loading = false;
      showAlert(err.msg);
    });
  }

  // 向服务器请求当前用户已创建的角色列表
  function getRoleList(pageNo) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.role_list,
      type: 'get',
      data: {
        pageNo: pageNo
      },
      success: function success(data) {
        dtd.resolve(data.result);
      },
      error: function error(msg, code) {
        dtd.reject({ msg: msg, code: code });
      }
    });
    return dtd.promise();
  }

  // 向服务器请求角色类型列表
  function getRoleType() {
    var dtd = $.Deferred();
    goAPI({
      url: _api.role_type,
      type: 'get',
      success: function success(data) {
        dtd.resolve(data.result);
      },
      error: function error(msg, code) {
        dtd.reject({ msg: msg, code: code });
      }
    });
    return dtd.promise();
  }

  // 向服务器获取角色详情
  function getRoleDetail(id) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.role_detail + '/' + id,
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

  // 创建或编辑角色
  function addRole(role) {
    var role_name = $('#role-name').val().trim();
    var confirm = true;
    if (role_name.length === 0) {
      $('.err-txt-role-name').text('角色名称不能为空');
      confirm = false;
    }
    if (!role_type_selected) {
      $('.err-txt-role-type').text('角色类型不能为空');
      confirm = false;
    }
    if (!confirm) {
      return;
    }
    if (role && role.id) {
      editRoleConfirm({
        id: role.id,
        name: role_name,
        roleId: role_type_selected,
        power: role_power || ''
      }, function () {
        var item = $('#list-box .permission-item[data-id="' + role.id + '"]');
        if (item.length > 0) {
          item.find('.role-name').text(role_name);
          item.find('.role-type').text(data_role_type[role_type_selected].name || '未知角色');
        }
        pm_role_edit.hide();
        showAlert('编辑成功');
        role.name = role_name;
        role.roleId = role_type_selected;
        role.power = role_power;
        initRoleDetail(role);
      }, function (msg, code) {
        showConfirm({
          str: msg
        });
      });
    } else {
      addRoleConfirm({
        name: role_name,
        roleId: role_type_selected,
        power: $('#add-per a:eq(0)').hasClass('active') ? 1 : ''
      }, function () {
        pm_role_add.hide();
        showAlert('创建成功');
        setTimeout(function () {
          list_page = 1;
          list_has_more = true;
          loadRoleList();
        }, 1500);
      }, function (msg, code) {
        showConfirm({
          str: msg
        });
      });
    }
  }

  // 删除角色
  function delRole(role) {
    delRoleConfirm(role.id, function () {
      var item = $('#list-box .permission-item[data-id="' + role.id + '"]');
      if (item.length > 0) {
        item.remove();
      }
      pm_role_edit.hide();
      pm_role_detail.hide();
      showAlert('删除成功');
    }, function (msg, code) {
      showConfirm({
        str: msg
      });
    });
  }

  // 向服务器提交创建角色
  function addRoleConfirm(dt, cbk, err) {
    if (loading) {
      return;
    }
    showLoad();
    goAPI({
      url: _api.role_add,
      data: dt,
      success: function success(data) {
        if ($.isFunction(cbk)) {
          cbk(data.result);
        }
      },
      error: function error(msg, code) {
        if ($.isFunction(err)) {
          err(msg, code);
        }
      },
      complete: function complete() {
        hideLoad();
        loading = false;
      }
    });
  }

  // 向服务器提交编辑角色
  function editRoleConfirm(dt, cbk, err) {
    if (loading) {
      return;
    }
    showLoad();
    goAPI({
      url: _api.role_edit,
      data: dt,
      success: function success(data) {
        if ($.isFunction(cbk)) {
          cbk(data.result);
        }
      },
      error: function error(msg, code) {
        if ($.isFunction(err)) {
          err(msg, code);
        }
      },
      complete: function complete() {
        hideLoad();
        loading = false;
      }
    });
  }

  // 向服务器提交删除角色
  function delRoleConfirm(dt, cbk, err) {
    if (loading) {
      return;
    }
    showLoad();
    goAPI({
      url: _api.role_del + '/' + dt,
      type: 'DELETE',
      success: function success(data) {
        if ($.isFunction(cbk)) {
          cbk(data.result);
        }
      },
      error: function error(msg, code) {
        if ($.isFunction(err)) {
          err(msg, code);
        }
      },
      complete: function complete() {
        hideLoad();
        loading = false;
      }
    });
  }
})();