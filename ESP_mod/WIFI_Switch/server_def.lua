local var = {}
answ = require ('answer_mod')
local cnt_reqCMD = 0 
local last_cnt_reqCMD = 255
local res_com = 53
local set_link = 17 
local flag_aswer = false
local tmr1 = tmr.create()

local ex_str = require 'func_STR'

sock_serv = nil

sv=net.createServer(net.TCP)

_G.flag_link_serv_def = false
_G.flag_stop_serv = false
local tmr3 = tmr.create()
------------------------------
--[[
        ]]
------------------------------
--Create Server

function create_serv_def() 
    wifi.setmode(wifi.STATIONAP)    -- устанавливаем режим точка доступа
    cfg={}
--    cfg.ssid="Dimmer_EZ" -- задаем имя сети
	cfg.ssid="Smart_Home_EZ" -- задаем имя сети
    cfg.pwd="12345678"      -- задаем пароль сети, WiFi точка не поднимется, если пароль короче 8 символов. По умолчанию IP адрес точки всегда 192.168.4.1
    wifi.ap.config(cfg)     -- загружаем настройки
    cfg = nil
    print ("Start server DEF!")
end  
--------------------------------------------------
function receiver_serv(sck, data)
    pcall(function ()
        res_com = string.byte(data, 69)
        cnt_reqCMD = string.byte(data, 68)

        len = string.byte(data, 66)
        _G.pathFL = string.sub(data, 70, 87)
        _G.strFL = string.sub(data, 88, (87+len))

--        print("get_data_for_android".." "..cnt_reqCMD.." "..res_com)
        while _G.modFL ~= "0" do print("wait..") end 
        if last_cnt_reqCMD ~= cnt_reqCMD then
            if res_com == 33 then
--                print("Запрос настроек ".._G.strFL.." файл ".._G.pathFL)
                _G.modFL = "4"
            end     
              
            if res_com == 61 then
--                print("Запись строки ".._G.strFL.." файл ".._G.pathFL)
                _G.modFL = "1"
                --dofile("write_setting.lua")
            end
            if res_com == 62 then
--                print("Удаление строки ".._G.strFL.." файл ".._G.pathFL)
                _G.modFL = "2"
                --dofile("delete_strFL.lua")
            end
            if res_com == 63 then
--                print("Удаление файла ".._G.pathFL)
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
--                print("Вкл. суточный режим")
                _G.mode_work = 0
                ex_str.write_new_data("val.lua", 'mod_= "', "0")
            end
            if res_com == 21 then
--                print("Вкл. недельный режим")
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
--            print("запрос статуса")
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
	tmr1:start()
    if res_com == set_link then   -- получена команда сброс модуля
--       print("comand set_link "..res_com) 
       pcall(function () answ.save_newdata(data) end)
        if string.byte(data, 62) ~= 51 then                 -- 51 - это сервер по умолчанию
            node.restart()
        end
    end
   
    collectgarbage()    --сборщик мусора
end
--------------------------------
function def_ser_disc(sck)
	print("OFF connection with Smart_Home_EZ!")
    sock_serv = nil
	tmr1:stop()
end
function connect_serv(sck) 
--     _G.flag_link_serv_def = true
--     tmr1:start()
end
---------------------------------
if sv then
    sv:listen(8888, function(conn, pl) 
        conn:on("receive", receiver_serv) print("get_data") 
        conn:on("connection", connect_serv) print(pl)         -- вызывается при подключении к серверу
        conn:on("disconnection", def_ser_disc) print(pl)    -- вызывается при отключении от сервера
--        conn:on("sent", def_ser_sent)print(pl)
        sock_serv = conn             -- сохраняем сокет в переменной sock_serv
        collectgarbage()    --сборщик мусора
    end)
end
--------------------------------------
function status_def_ESP()    
        local str1 = "_"
            if res_com ~= 33 and res_com ~= 52 then
                flag_aswer = false
                local dimUTF8 = {res_com, cnt_reqCMD}
--                print("res_com "..res_com)
                pcall(function () str1 = str1..answ.utf8_from(dimUTF8) end)
                pcall(function () sock_serv:send(answ.pars_data(str1)) end)   -- отправка ответа клиенту
--                print("str1= "..str1)
        end 
        str1 = nil
        collectgarbage() 
end

function clear_mem()
    answ = nil
    answer_mod = nil
    package.loaded['answer_mod']=nil 
    ex_str = nil
    func_STR = nil
    package.loaded['func_STR']=nil
    INIT_config = nil
    def_ser_disc = nil
    create_serv_def = nil
    receiver_serv = nil
    status_def_ESP = nil
    sv = nil
    collectgarbage() 
end
function status_server_stop()
    if _G.flag_stop_serv then
        pcall(function () sv:close() end)
        _G.flag_stop_serv = nil
        _G.flag_link_serv_def = nil
        print("Server def STOP!")
        clear_mem()
		tmr3:stop()
        clear_mem = nil
    end
end

create_serv_def()
create_serv_def = nil
	tmr1:register(350, tmr.ALARM_AUTO, function() status_def_ESP() end)
	tmr3:register(250, tmr.ALARM_AUTO, function() status_server_stop() end)
	tmr3:start()
collectgarbage() 
--------------------
