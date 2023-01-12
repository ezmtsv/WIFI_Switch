---------------nilnet.createServer(net.TCP) 
sock_serv = nil
local cnt_reqCMD = 0 
local res_com = 53
local flag_aswer = false

local last_cnt_reqCMD
local flag_wifi_con = false
local tmr0 = tmr.create()
local tmr1 = tmr.create()
local tmr4 = tmr.create()
serv = net.createServer(net.TCP, 300)    
local answ = require ('answer_mod')
local ex_str = require 'func_STR'
----------------debug
--------------------
conf = {}
conf = ex_str.get_init_CONF()
---------------
function status_res()    
        local str1 = "_"
        if flag_aswer == true then
--            flag_aswer_count = flag_aswer_count + 1
--            if flag_aswer_count > 2 then 
--                flag_aswer_count = 1
            if res_com ~= 33 and res_com ~= 52 then
                flag_aswer = false
                local dimUTF8 = {res_com, cnt_reqCMD}
                print("res_com "..res_com)
                pcall(function () str1 = str1..answ.utf8_from(dimUTF8) end)
                err = pcall(function () sock_serv:send(answ.pars_data(str1)) end)   -- отправка ответа клиенту
                print("str1= "..str1)
            end 
        end 
--        print("statusESP")
        str1 = nil
        collectgarbage() 
end
----------------debug---------
print(sock_serv)
--------------------------------------------------
function receiver_serv(sck, data)
    pcall(function ()
        res_com = string.byte(data, 69)
		cnt_reqCMD = string.byte(data, 68)
        --[[
        if res_com ~= 100 then
            bright = string.byte(data, 75)
			if bright == 10 then 
				_G.stateON = 1
			end
			if bright == 11 then 
				_G.stateON = 0
			end
        end
        --]]
		len = string.byte(data, 66)
		_G.pathFL = string.sub(data, 70, 87)
		_G.strFL = string.sub(data, 88, (87+len))
		while _G.modFL ~= "0" do print("wait..") end 
			if last_cnt_reqCMD ~= cnt_reqCMD then
				if res_com == 33 then
					print("Запрос настроек ".._G.strFL.." файл ".._G.pathFL)
					_G.modFL = "4"
				end     
              
				if res_com == 61 then
					print("Запись строки ".._G.strFL.." файл ".._G.pathFL)
					_G.modFL = "1"
				--dofile("write_setting.lua")
				end
				if res_com == 62 then
					print("Удаление строки ".._G.strFL.." файл ".._G.pathFL)
					_G.modFL = "2"
				--dofile("delete_strFL.lua")
				end
				if res_com == 63 then
					print("Удаление файла ".._G.pathFL)
					_G.modFL = "3"
				--dofile("delete_FL.lua")
				end
				if res_com == 10 then
					print("Вкл.")
				_G.stateON = 1
				end
				if res_com == 11 then
					print("Выкл.")
					_G.stateON = 0
				end
				if res_com == 20 then
					print("Вкл. суточный режим")
					_G.mode_work = 0
					ex_str.write_new_data("val.lua", 'mod_= "', "0")
				end
				if res_com == 21 then
					print("Вкл. недельный режим")
					_G.mode_work = 1
					ex_str.write_new_data("val.lua", 'mod_= "', "1")
				end		
				if res_com == 53 then
                    rtctime.set(0, 0)
					print("установка синхро с андроид ")
					-- print("_G.pathFL ".._G.pathFL.." _G.strFL ".._G.strFL.." + "..(tonumber(_G.pathFL) + tonumber(_G.strFL)))
				    rtctime.set(tonumber(_G.pathFL) + tonumber(_G.strFL), 0)
                    ex_str.write_new_data("val.lua", 'shif= "', _G.strFL)
                    flag_SYNC = 1
				end                         	
				last_cnt_reqCMD = cnt_reqCMD
			end	
			if res_com == 52 then
			print("запрос статуса")
			_G.modFL = "5"
			end 
		--[[
        print("запрос №: "..string.byte(data, 68))
        print("команда: "..res_com)
        len = string.byte(data, 66)
        print("длина строки: "..len)
        print("файл: "..string.sub(data, 70, 87))
        print("строка: "..string.sub(data, 88, (87+len)))
        --]]
        flag_aswer = true 
		--flag_aswer_count = 1
		
    end)
	if res_com == 17 then   -- получена команда сброс модуля
	       print("загружаем настройки по умолчанию")
           ex_str.write_new_data("val.lua", 'SSID= "', "Smart_Home_EZ")
           ex_str.write_new_data("val.lua", 'mode= "', "servDEF")
           ex_str.write_new_data("val.lua", 'cmd_= "', "work")
           node.restart()
	end
    collectgarbage()    --сборщик мусора
end
--------------------------------
function connect_serv(sck)
--    print("connect with DIMMER_EZ!"..tostring(sock_serv))
	print("connect with Smart_Home_EZ!"..tostring(sock_serv))
--    tmr.alarm(1, 350, 1, function() status_res()  end)     -- вызов функции status_def_ESP() каждые 350 mсек
--	tmr1:register(350, tmr.ALARM_AUTO, function() status_res() end)
	tmr1:start()
    collectgarbage()
end
--------------------------------
function disconnect_serv(sck)
--    print("OFF connection with DIMMER_EZ!")
	print("OFF connection with Smart_Home_EZ!")
    sock_serv = nil
	tmr1:stop() -- останавливаем таймер 1 в случае потери связи с клиентом
end
---------------------------------
---------------------------------
if serv then
    serv:listen(8888, function(conn, pl) 
        conn:on("receive", receiver_serv) print(pl) 
        conn:on("connection", connect_serv)         -- вызывается при подключении к серверу
        conn:on("disconnection", disconnect_serv) print(pl)    -- вызывается при отключении от сервера
--        conn:on("sent", def_ser_sent)print(pl)
        sock_serv = conn             -- сохраняем сокет в переменной sock_serv
        if sock_serv ~= nil then
 --           tmr1:register(350, tmr.ALARM_AUTO, function() status_res() end)
            tmr1:start()
            else print("serv = NIL")
        end
        collectgarbage()    --сборщик мусора
    end)
end
--------------------------------------
function reload_init_W(ip_adr_ser)
    pcall(function () _G.init_word = answ.initanswer_data(ip_adr_ser, flag_serv) end)
    print("_G.init_word ".._G.init_word)
end
---------------
function status_ESP() 
    if flag_wifi_con == false then      -- получаем IP адрес
       local ip_adr =  wifi.sta.getip()
       if ip_adr ~= nil then            -- IP адрес получен
        print("получен IP "..ip_adr)
        reload_init_W(ip_adr)
        reload_init_W = nil
        flag_wifi_con = true            -- при установки этого флага больше не заходим сюда
		tmr0:stop()
        status_server_stop = nil
       else
         print("ожидание получения IP")
       end 
    else
--         print("ожидание подключения к сети ")
    end 
---       print(wifi.sta.status())          -- получение статуса сети, если 5, то получен IP
end
----------------
function init_server()
    status_server_def = nil   -- освобождает память  
	   	
       wifi.setmode(wifi.STATION)
       wifi.sta.config{ssid = string.sub(conf.ssid, 8,#conf.ssid-1), pwd = string.sub(conf.pass, 8, #conf.pass-1)}
       wifi.sta.autoconnect(1)
	  tmr0:register(3000, tmr.ALARM_AUTO, function() status_ESP() end)  -- вызов функции status_ESP() каждые 3 сек 
	  tmr0:start()
end
---------------
function status_server_def()
    if _G.flag_link_serv_def==nil then
		tmr4:stop()
		init_server()
        init_server = nil
    else print(tostring(_G.flag_link_serv_def))    
    end
end

	tmr4:register(500, tmr.ALARM_AUTO, function() status_server_def() end)
	tmr4:start()
--------------
tmr1:register(350, tmr.ALARM_AUTO, function() status_res() end)
collectgarbage()
print(wifi.sta.getip()) 

