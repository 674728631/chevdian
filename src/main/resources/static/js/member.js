'use strict';

(function () {
  var tbox;
  var loading = false;
  var city_list; // 城市列表数据源
  var insurance_list; // 保险公司列表数据源
  var role_list; // 角色类型列表数据源
  var pm_car; // 车辆详情弹窗对象
  var pm_car_edit; // 编辑车辆详情弹窗对象
  var pm_car_pay; // 充值车辆弹窗对象
  var pm_agent; // 代理人详情弹窗对象
  var pm_agent_edit; // 编辑代理人详情弹窗对象
  var pm_channel; // 渠道详情弹窗对象
  var pm_channel_add; // 创建、编辑渠道详情弹窗对象
  var datapick_date; // 保险到期时间选择器
  var datapick_role; // 代理人角色类型选择器
  var datapick_city; // 城市选择器
  var datapick_insurance; // 保险公司选择器
  var insurance_time; // 保险到期时间
  var insurance_type; // 保险公司
  var role_type; // 代理人角色类型
  var city_area; // 城市地区
  var car_type = 1; // 车辆列表的显示模式（1：查看、编辑车辆；2：选择车辆并支付）
  var search_page = 1; // 搜索分页
  var search_loading = false; // 搜索加载中
  var search_more = false; // 搜索结果是否还有下一页
  var pay_level = {
    '1': {
      amount: 9,
      month: 1
    },
    '3': {
      amount: 99,
      month: 12
    },
    '4': {
      amount: 29,
      month: 3
    }
  };
  var page_type = 1;
  var page_data = {
    '1': {
      'api': _api.car_list,
      'tpl': juicer($('#car-list-tpl').html()),
      'loading': false,
      'inited': false,
      'more': true,
      'page': 1,
      'search': 1,
      'null_text': '暂无客户信息，您可邀请客户'
    },
    '2': {
      'api': _api.agent_list,
      'tpl': juicer($('#agent-list-tpl').html()),
      'loading': false,
      'inited': false,
      'more': true,
      'page': 1,
      'search': 20,
      'null_text': '暂无客户信息，您可邀请代理'
    },
    '3': {
      'api': _api.channel_list,
      'tpl': juicer($('#channel-list-tpl').html()),
      'loading': false,
      'inited': false,
      'more': true,
      'page': 1,
      'search': 10,
      'null_text': '暂无客户信息，您可创建渠道'
    }
  };
  // juicer函数，根据省市ID获取对应的省市名称
  var juicer_getCityName = function juicer_getCityName(pid, cid) {
    if (!city_list) {
      return '';
    }
    var province = city_list[pid];
    if (!province) {
      return '';
    }
    var city = province.value[cid];
    if (!city) {
      return province.name;
    }
    return province.name + city.name;
  };
  juicer.register('juicer_getCityName', juicer_getCityName);

  initPage();

  // 初始化页面
  function initPage() {
    showLoad();
    checkBind(function (user) {
      $('.container').html(juicer($('#main-tpl').html(), user || {}));

      tbox = tabbox({
        container: '.tabbox',
        height: '100%',
        pull: true,
        wheel_self: false,
        event_pullMove: function event_pullMove(dis, index) {
          if (dis >= 40) {
            $('.tabbox-refresh').eq(index).find('.txt').text('松开刷新');
            $('.tabbox-refresh').eq(index).find('.arr3').addClass('uturn');
          } else {
            $('.tabbox-refresh').eq(index).find('.txt').text('下拉刷新');
            $('.tabbox-refresh').eq(index).find('.arr3').removeClass('uturn');
          }
        },
        event_pullEnd: function event_pullEnd(dis, index) {
          if (dis >= 40) {
            $('.tabbox-refresh').eq(index).find('.txt').text('下拉刷新');
            $('.tabbox-refresh').eq(index).find('.arr3').removeClass('uturn');
            loadList(true);
          }
        },
        event_scrollBottom: function event_scrollBottom(index, dis) {
          if (page_data[page_type].loading || !page_data[page_type].more) return;
          loadList();
        },
        event_switch: function event_switch(index, lastIndex) {
          var $item = $('#menu-box .menu-item').eq(index);
          $item.addClass('active').siblings('.menu-item').removeClass('active');
          $('.btn-box').removeClass('active').eq(index).addClass('active');
          page_type = $item.data('value');
          if (!page_data[page_type].inited) {
            loadList(true);
          }
        }
      });
      // 初始自动切换至第一个选项卡
      tbox.switchTo($('#menu-box a[data-value="' + page_type + '"]').index(), { animation: false, event: true });

      // 切换选项卡
      $('#menu-box').on('click', '.menu-item', function () {
        var $this = $(this);
        if (!$this.hasClass('active')) {
          $this.addClass('active').siblings('.menu-item').removeClass('active');
          tbox.switchTo($this.index());
        }
      });

      // 搜索结果滑动到底部加载下一页
      var $search = $('#search-result');
      var $search_content = $('#search-result-content');
      var dis = $search.scrollTop();
      $search.on('scroll', function () {
        if (search_more && !search_loading && dis <= $search.scrollTop() && $search.scrollTop() + $search.height() + 20 > $search_content.height()) {
          searchData();
        }
        dis = $search.scrollTop();
      });

      // 点击显示搜索界面
      $('#btn-search').on('click', function () {
        $('#search-result-content').html('');
        $('#search-content').val('');
        $('#search-box').removeClass('hide');
        $('.btn-car-cancel').click();
      });

      // 点击隐藏搜索界面
      $('#btn-search-cancel').on('click', function () {
        $('#search-box').addClass('hide');
      });

      // 输入回车开始搜索
      $('#search-content').on('keydown', function (e) {
        if (e.keyCode === 13) {
          searchData(true);
        }
      });

      // 点击开始搜索
      $('#btn-search-confirm').on('click', function () {
        searchData(true);
      });

      // 点击切换车辆列表的显示模式
      $('.btn-car-recharge').on('click', function () {
        if ($('#car-box .list-item').length === 0) {
          showAlert('暂无客户，无法充值');
          return;
        }
        $('.btn-car-box').addClass('type-pay');
        car_type = 2;
      });
      $('.btn-car-cancel').on('click', function () {
        $('.btn-car-box').removeClass('type-pay');
        $('#car-box .list-item').removeClass('checked');
        car_type = 1;
      });

      // 点击为车辆充值
      $('.btn-car-pay').on('click', function () {
        var car_list = $('#car-box .list-item.checked');
        if (car_list.length === 0) {
          showAlert('请至少选择一辆车');
          return;
        }
        if (!pm_car_pay) {
          pm_car_pay = pushMenu({
            btn_yes: {
              str: '确定',
              click_close: false,
              event_click: function event_click() {}
            },
            btn_cancel: {
              str: '取消'
            }
          });
        }
        pm_car_pay.show({
          node: $('#car-pay-tpl').html()
        });
        // 切换充值金额
        $('#pay-level-box').on('click', '.level-item', function () {
          var $this = $(this);
          if (!$this.hasClass('active')) {
            $this.addClass('active').siblings().removeClass('active');
          }
        });
      });

      // 点击创建渠道
      $('.btn-channel-add').on('click', function () {
        if (loading) {
          return;
        }
        loading = true;
        showLoad();
        $.when(getCityList(), getRoleList()).done(function (city, role) {
          loading = false;
          hideLoad();
          if (city) {
            city_list = initCityList(city);
          }
          if (role) {
            role_list = initRoleList(role);
          }
          role_type = null;
          city_area = null;
          if (!datapick_city) {
            datapick_city = datapicker2({
              data: city_list,
              active_level: 2,
              event_ok: function event_ok(data) {
                city_area = [data[0].value, data[1].value];
                $('.channel-city').val(data[0].name + data[1].name);
                $('.err-txt-channel-city').text('');
              }
            });
          }
          if (!datapick_role) {
            datapick_role = datapicker2({
              data: role_list,
              active_level: 1,
              event_ok: function event_ok(data) {
                role_type = data[0].value;
                $('.channel-role').val(data[0].name);
                $('.err-txt-channel-role').text('');
              }
            });
          }
          if (!pm_channel_add) {
            pm_channel_add = pushMenu({
              btn_yes: {
                str: '确定',
                click_close: false,
                event_click: function event_click() {
                  if (loading) {
                    return;
                  }
                  var name = $('.channel-name').val().trim();
                  var phone = $('.channel-phone').val().trim();
                  var confirm = true;
                  if (name.length === 0) {
                    $('.err-txt-channel-name').text('渠道名称不能为空');
                    confirm = false;
                  }
                  if (phone.length === 0) {
                    $('.err-txt-channel-phone').text('手机号码不能为空');
                    confirm = false;
                  } else if (!validatePhone(phone, true)) {
                    $('.err-txt-channel-phone').text('手机号码格式不正确');
                    confirm = false;
                  }
                  if (!role_type) {
                    $('.err-txt-channel-role').text('角色类型不能为空');
                    confirm = false;
                  }
                  if (!city_area) {
                    $('.err-txt-channel-city').text('地区不能为空');
                    confirm = false;
                  }
                  if (!confirm) {
                    return;
                  }
                  loading = true;
                  showLoad();
                  addChannelDetail({
                    userName: name,
                    userPn: phone,
                    roleId: role_type,
                    cityId: city_area[1]
                    //status: 1
                  }).done(function (data) {
                    loading = false;
                    hideLoad();
                    pm_channel_add.hide();
                    showAlert('创建成功');
                    setTimeout(function () {
                      loadList(true);
                    }, 1500);
                  }).fail(function (err) {
                    loading = false;
                    hideLoad();
                    showConfirm({
                      str: err.msg
                    });
                  });
                }
              },
              btn_cancel: {
                str: '取消'
              }
            });
          }
          pm_channel_add.show({
            node: juicer($('#channel-add-tpl').html(), null)
          });
          // 点击切换冻结状态
          $('.btn-channel-status').off('click').on('click', function () {
            $(this).addClass('active').siblings('a').removeClass('active');
            if ($(this).hasClass('btn-channel-enable')) {
              $('.channel-disable-box').addClass('hide');
            } else {
              $('.channel-disable-box').removeClass('hide');
            }
          });
          // 点击显示角色类型选择器
          $('.channel-role').off('click').on('click', function () {
            datapick_role.show(role_type);
          });
          // 点击显示地区选择器
          $('.channel-city').off('click').on('click', function () {
            datapick_city.show(city_area || null);
          });
          // 输入框输入检测
          $('.channel-name').off('blur').on('blur', function () {
            var val = $(this).val();
            var err = $('.err-txt-channel-name');
            if (val.length > 0) {
              err.text('');
            }
          });
          $('.channel-phone').on('blur', function () {
            var val = $(this).val();
            var err = $('.err-txt-channel-phone');
            if (val.length > 0 && !validatePhone(val, true)) {
              err.text('手机号格式不正确');
            } else {
              err.text('');
            }
          });
        }).fail(function (err) {
          loading = false;
          hideLoad();
          showAlert(err.msg);
        });
      });

      $('#container').on('click', '.list-item', function () {
        var item = $(this);
        if (item.hasClass('list-item-car')) {
          if (car_type == 1) {
            // 点击打开车辆详情弹窗
            var id = item.data('id');
            if (!id) {
              showAlert('未找到该车辆的ID');
              return;
            }
            if (loading) {
              return;
            }
            loading = true;
            showLoad();
            $.when(getCarDetail(id), getInsuranceList()).done(function (car, insurance) {
              loading = false;
              hideLoad();
              if (insurance) {
                insurance_list = initInsuranceList(insurance);
              }
              initCarDetail(car);
            }).fail(function (err) {
              loading = false;
              hideLoad();
              showAlert(err.msg);
            });
          } else {
            // 点击勾选要充值的车辆
            if (item.hasClass('checked')) {
              item.removeClass('checked');
            } else {
              var id = item.data('id');
              if (id) {
                item.addClass('checked');
              } else {
                showAlert('未找到该车辆的ID');
              }
            }
          }
        } else if (item.hasClass('list-item-agent')) {
          var id = item.data('id');
          if (!id) {
            showAlert('未找到该代理人的ID');
            return;
          }
          if (loading) {
            return;
          }
          loading = true;
          showLoad();
          $.when(getAgentDetail(id), getCityList(), getRoleList()).done(function (agent, city, role) {
            loading = false;
            hideLoad();
            if (city) {
              city_list = initCityList(city);
            }
            if (role) {
              role_list = initRoleList(role);
            }
            initAgentDetail(agent);
          }).fail(function (err) {
            loading = false;
            hideLoad();
            showAlert(err.msg);
          });
        } else if (item.hasClass('list-item-channel')) {
          var id = item.data('id');
          if (!id) {
            showAlert('未找到该渠道的ID');
            return;
          }
          if (loading) {
            return;
          }
          loading = true;
          showLoad();
          $.when(getChannelDetail(id), getCityList(), getRoleList()).done(function (channel, city, role) {
            loading = false;
            hideLoad();
            if (city) {
              city_list = initCityList(city);
            }
            if (role) {
              role_list = initRoleList(role);
            }
            initChannelDetail(channel);
          }).fail(function (err) {
            loading = false;
            hideLoad();
            showAlert(err.msg);
          });
        }
      });

      //初次加载列表
      loadList(true);
    }, function (msg, code) {
      hideLoad();
      setErrorPage($('.container'), msg, code, initPage);
    });
  }

  // 初始化车辆详情弹窗
  function initCarDetail(car) {
    if (!pm_car) {
      pm_car = pushMenu({
        btn_yes: {
          str: car.nameCarOwner ? '修改信息' : '完善信息',
          click_close: false,
          event_click: function event_click(car) {
            insurance_time = car.endTime ? new Date(car.endTime) : null;
            insurance_type = car.insuranceId || null;
            if (!datapick_date) {
              datapick_date = datapicker2({
                data: createDateSource(2010, 2100),
                active_level: 3,
                event_ok: function event_ok(data) {
                  insurance_time = new Date(data[0].value, data[1].value - 1, data[2].value, 0, 0, 0);
                  $('.car-insurance-time').val(insurance_time.formatDate('yyyy-MM-dd hh:mm:ss'));
                  $('.err-txt-car-insurance-time').text('');
                }
              });
            }
            if (!datapick_insurance) {
              datapick_insurance = datapicker2({
                data: insurance_list,
                active_level: 1,
                event_ok: function event_ok(data) {
                  insurance_type = data[0].value;
                  $('.car-insurance-name').val(data[0].name);
                  $('.err-txt-car-insurance-name').text('');
                }
              });
            }
            if (!pm_car_edit) {
              pm_car_edit = pushMenu({
                btn_yes: {
                  str: '确定',
                  click_close: false,
                  event_click: function event_click() {
                    if (loading) {
                      return;
                    }
                    var name = $('.car-owner-name').val().trim();
                    var confirm = true;
                    if (name.length === 0) {
                      $('.err-txt-car-owner-name').text('车主姓名不能为空');
                      confirm = false;
                    }
                    if (!insurance_type) {
                      $('.err-txt-car-insurance-name').text('保险投保公司不能为空');
                      confirm = false;
                    }
                    if (!insurance_time) {
                      $('.err-txt-car-insurance-time').text('保险到期日不能为空');
                      confirm = false;
                    }
                    if (!confirm) {
                      return;
                    }
                    loading = true;
                    showLoad();
                    editCarDetail({
                      carId: car.carId,
                      nameCarOwner: name,
                      insuranceId: insurance_type,
                      endTime: insurance_time.formatDate('yyyy-MM-dd hh:mm:ss')
                    }).done(function (data) {
                      loading = false;
                      hideLoad();
                      showAlert('提交成功');
                      pm_car_edit.hide();
                      car.insurance = insurance_list[insurance_type].name;
                      car.insuranceId = insurance_type;
                      car.nameCarOwner = name;
                      car.endTime = insurance_time.formatDate('yyyy-MM-dd hh:mm:ss');
                      initCarDetail(car);
                    }).fail(function (err) {
                      loading = false;
                      hideLoad();
                      showConfirm({
                        str: err.msg
                      });
                    });
                  }
                },
                btn_cancel: {
                  str: '取消'
                }
              });
            }
            pm_car_edit.show({
              node: juicer($('#car-edit-tpl').html(), car)
            });
            // 点击显示时间选择器
            $('.car-insurance-time').off('click').on('click', function () {
              datapick_date.show(insurance_time ? [insurance_time.formatDate('yyyy'), insurance_time.formatDate('M'), insurance_time.formatDate('d')] : null);
            });
            // 点击显示保险公司选择器
            $('.car-insurance-name').off('click').on('click', function () {
              datapick_insurance.show(insurance_type || null);
            });
            // 输入框输入检测
            $('.car-owner-name').off('blur').on('blur', function () {
              var val = $(this).val();
              var err = $('.err-txt-car-owner-name');
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
    pm_car.show({
      node: juicer($('#car-detail-tpl').html(), car),
      data: car,
      btn_yes: {
        str: car.nameCarOwner ? '修改信息' : '完善信息'
      }
    });
  }

  // 初始化代理人详情弹窗
  function initAgentDetail(agent) {
    if (!pm_agent) {
      pm_agent = pushMenu({
        btn_yes: {
          str: '编辑',
          click_close: false,
          event_click: function event_click(agent) {
            var role_list_temp = $.extend({}, role_list);
            if (agent.roleId) {
              role_list_temp[agent.roleId] = {
                name: agent.roleName || '未知角色'
              };
            }
            role_type = agent.roleId || null;
            city_area = agent.provinceId && agent.cityId ? [agent.provinceId, agent.cityId] : null;
            if (!datapick_city) {
              datapick_city = datapicker2({
                data: city_list,
                active_level: 2,
                event_ok: function event_ok(data) {
                  city_area = [data[0].value, data[1].value];
                  $('.agent-city').val(data[0].name + data[1].name);
                  $('.err-txt-agent-city').text('');
                }
              });
            }
            if (!datapick_role) {
              datapick_role = datapicker2({
                data: role_list,
                active_level: 1,
                event_ok: function event_ok(data) {
                  role_type = data[0].value;
                  $('.agent-role').val(data[0].name);
                  $('.err-txt-agent-role').text('');
                }
              });
            }
            if (!pm_agent_edit) {
              pm_agent_edit = pushMenu({
                btn_yes: {
                  str: '确定',
                  click_close: false,
                  event_click: function event_click() {
                    if (loading) {
                      return;
                    }
                    var name = $('.agent-name').val().trim();
                    var phone = $('.agent-phone').val().trim();
                    var confirm = true;
                    if (name.length === 0) {
                      $('.err-txt-agent-name').text('代理人姓名不能为空');
                      confirm = false;
                    }
                    if (phone.length === 0) {
                      $('.err-txt-agent-phone').text('手机号码不能为空');
                      confirm = false;
                    } else if (!validatePhone(phone, true)) {
                      $('.err-txt-agent-phone').text('手机号码格式不正确');
                      confirm = false;
                    }
                    if (!role_type) {
                      $('.err-txt-agent-role').text('角色类型不能为空');
                      confirm = false;
                    }
                    if (!city_area) {
                      $('.err-txt-agent-city').text('地区不能为空');
                      confirm = false;
                    }
                    if (!confirm) {
                      return;
                    }
                    loading = true;
                    showLoad();
                    editAgentDetail({
                      id: agent.id,
                      userName: name,
                      userPn: phone,
                      roleId: role_type,
                      cityId: city_area[1]
                      //status: 1
                    }).done(function (data) {
                      var item = $('#container .list-item-agent[data-id="' + agent.id + '"]');
                      if (item.length > 0) {
                        item.find('.agent-item-name').text(name);
                        item.find('.agent-item-phone').text(phone);
                        item.find('.agent-item-role').text(role_list_temp[role_type].name);
                      }
                      loading = false;
                      hideLoad();
                      showAlert('提交成功');
                      pm_agent_edit.hide();
                      agent.userName = name;
                      agent.userPn = phone;
                      agent.roleId = role_type;
                      agent.provinceId = city_area[0];
                      agent.cityId = city_area[1];
                      initAgentDetail(agent);
                    }).fail(function (err) {
                      loading = false;
                      hideLoad();
                      showConfirm({
                        str: err.msg
                      });
                    });
                  }
                },
                btn_cancel: {
                  str: '取消'
                }
              });
            }
            pm_agent_edit.show({
              node: juicer($('#agent-edit-tpl').html(), agent)
            });
            // 点击切换冻结状态
            $('.btn-agent-status').off('click').on('click', function () {
              $(this).addClass('active').siblings('a').removeClass('active');
              if ($(this).hasClass('btn-agent-enable')) {
                $('.agent-disable-box').addClass('hide');
              } else {
                $('.agent-disable-box').removeClass('hide');
              }
            });
            // 点击显示角色类型选择器
            $('.agent-role').off('click').on('click', function () {
              datapick_role.show(role_type);
            });
            // 点击显示地区选择器
            $('.agent-city').off('click').on('click', function () {
              datapick_city.show(city_area || null);
            });
            // 输入框输入检测
            $('.agent-name').off('blur').on('blur', function () {
              var val = $(this).val();
              var err = $('.err-txt-agent-name');
              if (val.length > 0) {
                err.text('');
              }
            });
            $('.agent-phone').on('blur', function () {
              var val = $(this).val();
              var err = $('.err-txt-agent-phone');
              if (val.length > 0 && !validatePhone(val, true)) {
                err.text('手机号格式不正确');
              } else {
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
    pm_agent.show({
      data: agent,
      node: juicer($('#agent-detail-tpl').html(), agent)
    });
  }

  // 初始化渠道详情弹窗
  function initChannelDetail(channel) {
    if (!pm_channel) {
      pm_channel = pushMenu({
        btn_yes: {
          str: '编辑',
          click_close: false,
          event_click: function event_click(channel) {
            var role_list_temp = $.extend({}, role_list);
            if (channel.roleId) {
              role_list_temp[channel.roleId] = {
                name: channel.roleName || '未知角色'
              };
            }
            role_type = channel.roleId || null;
            city_area = channel.provinceId && channel.cityId ? [channel.provinceId, channel.cityId] : null;
            if (!datapick_city) {
              datapick_city = datapicker2({
                data: city_list,
                active_level: 2,
                event_ok: function event_ok(data) {
                  city_area = [data[0].value, data[1].value];
                  $('.channel-city').val(data[0].name + data[1].name);
                  $('.err-txt-channel-city').text('');
                }
              });
            }
            if (!datapick_role) {
              datapick_role = datapicker2({
                data: role_list,
                active_level: 1,
                event_ok: function event_ok(data) {
                  role_type = data[0].value;
                  $('.channel-role').val(data[0].name);
                  $('.err-txt-channel-role').text('');
                }
              });
            }
            if (!pm_channel_add) {
              pm_channel_add = pushMenu({
                btn_yes: {
                  str: '确定',
                  click_close: false,
                  event_click: function event_click() {
                    if (loading) {
                      return;
                    }
                    var name = $('.channel-name').val().trim();
                    var phone = $('.channel-phone').val().trim();
                    var confirm = true;
                    if (name.length === 0) {
                      $('.err-txt-channel-name').text('渠道名称不能为空');
                      confirm = false;
                    }
                    if (phone.length === 0) {
                      $('.err-txt-channel-phone').text('手机号码不能为空');
                      confirm = false;
                    } else if (!validatePhone(phone, true)) {
                      $('.err-txt-channel-phone').text('手机号码格式不正确');
                      confirm = false;
                    }
                    if (!role_type) {
                      $('.err-txt-channel-role').text('角色类型不能为空');
                      confirm = false;
                    }
                    if (!city_area) {
                      $('.err-txt-channel-city').text('地区不能为空');
                      confirm = false;
                    }
                    if (!confirm) {
                      return;
                    }
                    loading = true;
                    showLoad();
                    addChannelDetail({
                      id: channel.id,
                      userName: name,
                      userPn: phone,
                      roleId: role_type,
                      cityId: city_area[1]
                      //status: 1
                    }).done(function (data) {
                      var item = $('#container .list-item-channel[data-id="' + channel.id + '"]');
                      if (item.length > 0) {
                        item.find('.channel-item-name').text(name);
                        item.find('.channel-item-phone').text(phone);
                        item.find('.channel-item-role').text(role_list_temp[role_type].name);
                      }
                      loading = false;
                      hideLoad();
                      showAlert('提交成功');
                      pm_channel_add.hide();
                      channel.userName = name;
                      channel.userPn = phone;
                      channel.roleId = role_type;
                      channel.roleName = role_list_temp[role_type].name;
                      channel.provinceId = city_area[0];
                      channel.cityId = city_area[1];
                      initChannelDetail(channel);
                    }).fail(function (err) {
                      loading = false;
                      hideLoad();
                      showConfirm({
                        str: err.msg
                      });
                    });
                  }
                },
                btn_cancel: {
                  str: '取消'
                }
              });
            }
            pm_channel_add.show({
              zIndex: 1000,
              node: juicer($('#channel-add-tpl').html(), channel)
            });
            // 点击切换冻结状态
            $('.btn-channel-status').off('click').on('click', function () {
              $(this).addClass('active').siblings('a').removeClass('active');
              if ($(this).hasClass('btn-channel-enable')) {
                $('.channel-disable-box').addClass('hide');
              } else {
                $('.channel-disable-box').removeClass('hide');
              }
            });
            // 点击显示角色类型选择器
            $('.channel-role').off('click').on('click', function () {
              datapick_role.show(role_type);
            });
            // 点击显示地区选择器
            $('.channel-city').off('click').on('click', function () {
              datapick_city.show(city_area || null);
            });
            // 输入框输入检测
            $('.channel-name').off('blur').on('blur', function () {
              var val = $(this).val();
              var err = $('.err-txt-channel-name');
              if (val.length > 0) {
                err.text('');
              }
            });
            $('.channel-phone').on('blur', function () {
              var val = $(this).val();
              var err = $('.err-txt-channel-phone');
              if (val.length > 0 && !validatePhone(val, true)) {
                err.text('手机号格式不正确');
              } else {
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
    pm_channel.show({
      data: channel,
      node: juicer($('#channel-detail-tpl').html(), channel)
    });
  }

  // 加载列表
  function loadList(emptyAll) {
    if (page_data[page_type].loading) return;
    showLoad();
    page_data[page_type].loading = true;
    if (emptyAll) {
      page_data[page_type].page = 1;
      page_data[page_type].more = true;
    }
    getList(page_data[page_type].api, {
      pageNo: page_data[page_type].page
    }, function (data) {
      hideLoad();
      page_data[page_type].loading = false;
      page_data[page_type].inited = true;
      if (emptyAll) {
        tbox.activeTab().find('.list-box').empty();
      }
      if (data.total == 0) {
        page_data[page_type].more = true;
        tbox.activeTab().addClass('full');
        tbox.activeTab().find('.list-box').showRefresh({
          str: page_data[page_type].null_text,
          fixed: true
        });
      } else {
        page_data[page_type].page += 1;
        tbox.activeTab().removeClass('full');
        tbox.activeTab().find('.list-box').append(page_data[page_type].tpl.render(data.list));
        page_data[page_type].more = data.hasNextPage;
      }
    }, function (msg, code) {
      hideLoad();
      page_data[page_type].loading = false;
      if (page_data[page_type].inited) {
        showAlert(msg);
      } else {
        tbox.activeTab().addClass('full');
        setErrorPage(tbox.activeTab().find('.list-box'), msg, code, function () {
          loadList(emptyAll);
        });
      }
    });
  }

  // 向服务器获取列表
  function getList(api, dt, cbk, err) {
    goAPI({
      url: api,
      data: dt,
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

  // 搜索
  function searchData(isClear) {
    if (search_loading) {
      return;
    }
    var val = $('#search-content').val().trim();
    if (val.length === 0) {
      showAlert('搜索内容不能为空');
      return;
    }
    if (isClear) {
      search_page = 1;
      search_more = true;
    }
    showLoad();
    search_loading = true;
    getSearchResult({
      searchInfo: val,
      searchType: page_data[page_type].search,
      pageNo: search_page
    }).done(function (data) {
      hideLoad();
      search_loading = false;
      if (data.total == 0) {
        $('#search-result-content').html('').showRefresh({
          str: '暂时没有查询到相关信息',
          fixed: true
        });
      } else if (search_page === 1) {
        $('#search-result-content').html(page_data[page_type].tpl.render(data.list));
      } else {
        $('#search-result-content').append(page_data[page_type].tpl.render(data.list));
      }
      search_more = data.hasNextPage;
      search_page += 1;
    }).fail(function (err) {
      hideLoad();
      search_loading = false;
      if (search_page === 1) {
        setErrorPage($('#search-result-content'), err.msg, err.code, searchData);
      } else {
        showAlert(err.msg);
      }
    });
  }

  // 向服务器获取搜索结果
  function getSearchResult(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.search,
      data: dt,
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

  // 向服务器获取车辆详情
  function getCarDetail(id) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.car_detail + '/' + id,
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

  // 向服务器提交编辑车辆信息
  function editCarDetail(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.car_edit,
      data: dt,
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

  // 向服务器获取代理人详情
  function getAgentDetail(id) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.agent_detail + '/' + id,
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

  // 向服务器提交编辑代理人信息
  function editAgentDetail(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.agent_edit,
      data: dt,
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

  // 向服务器获取渠道详情
  function getChannelDetail(id) {
    var dtd = $.Deferred();
    goAPI({
      url: _api.channel_detail + '/' + id,
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

  // 向服务器提交创建、编辑渠道信息
  function addChannelDetail(dt) {
    var dtd = $.Deferred();
    goAPI({
      url: dt.id ? _api.channel_edit : _api.channel_add,
      data: dt,
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

  // 向服务器获取保险公司列表
  function getInsuranceList() {
    var dtd = $.Deferred();
    if (insurance_list) {
      dtd.resolve(null);
      return dtd.promise();
    }
    goAPI({
      url: _api.insurance_list,
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

  // 向服务器获取地区列表
  function getCityList() {
    var dtd = $.Deferred();
    if (city_list) {
      dtd.resolve(null);
      return dtd.promise();
    }
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

  // 向服务器获取角色类型列表
  function getRoleList() {
    var dtd = $.Deferred();
    if (role_list) {
      dtd.resolve(null);
      return dtd.promise();
    }
    goAPI({
      url: _api.role_list,
      type: 'get',
      success: function success(data) {
        dtd.resolve(data.result.list);
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

  // 构建保险公司选择器的数据源
  function initInsuranceList(arr) {
    var obj = {};
    if ($.isArray(arr)) {
      arr.map(function (item) {
        obj[item.id] = {
          name: item.insurance || '未知保险公司'
        };
      });
    }
    return obj;
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

  // 构建角色类型选择器的数据源
  function initRoleList(arr) {
    var obj = {};
    if ($.isArray(arr)) {
      arr.map(function (item) {
        obj[item.id] = {
          name: item.name || '未知角色'
        };
      });
    }
    return obj;
  }
})();