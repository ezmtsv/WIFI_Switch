---------------------------------
local my_pin_nummber = 4
--local pin_PWM = 3;
--pwm.setup(pin_PWM, 1000, 0)
--pwm.start(pin_PWM)
tmr_pwm = tmr.create()
local ex_string = require 'func_STR'
_G.stateON = 0
local prev_state = 0;
local state_SYNC = 64;         -- обеспечивает при старте задержку перед синхронизацией на 10,8 сек после отключения сервера по умолчанию(_G.flag_link_serv_def == nil)
flag_SYNC = 0
_G.mode_work = 0;

 _G.pathFL = ""
 _G.strFL = ""
 _G.modFL = "0"
--[[
time_week[1][1] = "1:14.15.00"      -- воскресенье
time_week[1][2] = "0:14.17.00"

time_week[7][1] = "1:14.15.00"  -- суббота
time_week[7][2] = "0:14.17.00" ]]--
----------------debug
--[[
IO index ESP8266 pin
0 GPIO16
1 GPIO5
2 GPIO4
3 GPIO0
4 GPIO2
5 GPIO14
6 GPIO12
7 GPIO13
8 GPIO15

]]--
--------------------
gpio.mode (my_pin_nummber, gpio.OUTPUT)
gpio.serout (my_pin_nummber, gpio.HIGH, {500000,300000}, 5, 1) -- 5 кол-во повторов
----------------- WIFI----------------------
local conf_var = {}
conf_var = ex_string.get_init_CONF()

-- print(conf_var.cmd)
-- print("shift time "..conf_var.shift_time)
-- print("__ "..string.sub(conf_var.shift_time,7, #conf_var.shift_time))
 tmr_UTC = string.sub(conf_var.shift_time,8, #conf_var.shift_time-1)
if rawequal(conf_var.modONOF, 'mod_= "0"') == false then
        _G.mode_work = 1
        print("режим неделя ".._G.mode_work)
    else
        _G.mode_work = 0
        print("режим сутки ".._G.mode_work)
         
end


package.loaded['func_STR']=nil


if rawequal(conf_var.cmd, 'cmd_= "work"') == false then
    dofile("WI_FI_reconect.lua") print("__reconect")
    else 
--    print("___config")
    dofile("server_def.lua")
    dofile("WI_FI_config.lua")   
end

function sync_time()	
  ----------------------  синхронизация с сервером точного времени
--    print("start synchro..") 
    sntp.sync("ntp1.stratum2.ru",
        function(sec, usec, server, info)
--            local tmr_UTC = 10800                     -- смещение для часового пояса UTC+3
            rtctime.set(0, 0)
            print('sync', sec, usec, server)
            rtctime.set(sec + tmr_UTC, 0)       -- устанавливаем время плюс смещение для часового пояса
            state_SYNC = 200
			flag_SYNC = 1
         end,
        function()
        state_SYNC = 0
        print('failed!')
        end
    )
end
--------------------------------------------------------
function set_PWM()
--[[
	local sst
	local sst_data
--]] 
	if _G.flag_link_serv_def == nil and flag_SYNC ~= 1 then		-- для отключения синхронизации при отсутствии подключения к внешней сети	
		if state_SYNC > 100  then                               -- при отсутствии синхронизации каждые 0,5мин попытка синхронизации
			state_SYNC = 0
			pcall(function () sync_time() end)  
			else 
		-- print("timer Wored "..state_SYNC)  
				state_SYNC = state_SYNC + 1
		end
	end
    tm = rtctime.epoch2cal(rtctime.get())       -- получаем текущее время
--    print("tm = ", tm["sec"])
	if prev_state ~= _G.stateON then
		if _G.stateON == 1 then
			gpio.write (my_pin_nummber, gpio.LOW)
 --           print("Load ON ")
        end	
		if _G.stateON == 0 then 
			gpio.write (my_pin_nummber, gpio.HIGH)
--            print("Load OFF")
		end
--        rtctime.set( 1615532751 - 10800, 0)
--        rtctime.set(0, 0)
--        print(string.format("%02d.%02d.%04d %02d:%02d:%02d", tm["day"], tm["mon"], tm["year"], tm["hour"], tm["min"], tm["sec"]))
--        print("flag_SYNC "..flag_SYNC ) 
--        print("_G.modFL ".._G.modFL ) 
--        print("_G.mode_work ".._G.mode_work) 
--        print("state_SYNC "..state_SYNC)
		prev_state = _G.stateON	
	end	
     --print("sec"..tm["sec"])
     
--    if tm["hour"] == 0 and tm["min"] == 0 and tm["sec"] == 0 then      -- синхронизация раз в суткм в 0ч.0м.0сек.
--            state_SYNC = 0
--    end

    if _G.modFL == "0" then                 --- если не было команды от приложения и в данный момент не происходит операции записи или редактирования файла
--	    if state_SYNC == 200 then
		if flag_SYNC == 1 then
		    if _G.mode_work == 0 then
			    dofile("work_day.lua")
		    else
			    dofile("work_schedule.lua")
		    end	 
 	    end
    end
    if _G.modFL == "1" then
        dofile("write_setting.lua")
    end
    if _G.modFL == "2" then
        dofile("delete_strFL.lua")
    end
    if _G.modFL == "3" then
        dofile("delete_FL.lua")
    end    
    if _G.modFL == "4" then
        dofile("send_setting.lua")
    end  
     if _G.modFL == "5" then
        dofile("send_sysTIME.lua")
    end     
            
-- print(state_SYNC)
collectgarbage()
 ------------------------------------   
    
end
    rtctime.set( 1604064427 + 10800, 0)                             -- установка произвольной даты при отсутствии синхро
 --   read_schedule()
 -- rtctime.set(1734280035, 0) 
 -- print(string.format("%02d.%02d.%04d %02d:%02d:%02d   %02d", tm["day"], tm["mon"], tm["year"], tm["hour"], tm["min"], tm["sec"], tm["wday"]))

    tmr_pwm:register(300, tmr.ALARM_AUTO, function() set_PWM() end) 
    tmr_pwm:start()
    collectgarbage()
