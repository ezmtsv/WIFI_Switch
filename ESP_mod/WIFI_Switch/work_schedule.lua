
--------------------------------------------------------
local tm_cur = rtctime.epoch2cal(rtctime.get())       -- получаем текущее время
local tm_str = ""..tm["hour"].."."..tm["min"].."."..tm["sec"].."\n"
local str_parData = tm["day"].."."..tm["mon"].."."..tm["year"].."."
     str_parData = str_parData..tm_str
 --tm_str - текущие параметры времени, str_parData -  параметры даты
local cnt_pos = 0
local strtmp
local str_data
local my_pin_nummber = 4

        _G.modFL = 10
        path_ = "valueweek_day"..tm_cur["wday"]..".lua"
  
    if file.open(path_, "r") ~= nil then
        cnt_pos = file.readline()           -- считываем кол-во записей
        if cnt_pos~= nil then
            for i = 1, cnt_pos do
                str_data = file.readline()          -- считываем текущую запись
                strtmp = "1:"..tm_str 
                if rawequal(str_data, strtmp) == true then
                    print("!!ON!!")
                     _G.stateON = 1
                    gpio.write (my_pin_nummber, gpio.LOW)
                end
                strtmp = "0:"..tm_str 
                if rawequal(str_data, strtmp) == true then
                    print("!!OFF!!")
                    _G.stateON = 0
                    gpio.write (my_pin_nummber, gpio.HIGH)
                end         
            end
        end
        file.close()
    end
    path_ = "value_schedule.lua"                -- работаем по дате
    if file.open(path_, "r") ~= nil then
        cnt_pos = file.readline()           -- считываем кол-во записей
        if cnt_pos~= nil then
            for i = 1, cnt_pos do
                str_data = file.readline()          -- считываем текущую запись
                strtmp = "1:"..str_parData 
                if rawequal(str_data, strtmp) == true then
                    print("!!ON!!")
                    _G.stateON = 1
                    gpio.write (my_pin_nummber, gpio.LOW)
                end
                strtmp = "0:"..str_parData 
                if rawequal(str_data, strtmp) == true then
                    print("!!OFF!!")
                    _G.stateON = 0
                    gpio.write (my_pin_nummber, gpio.HIGH)
                end         
            end
        end
        file.close()
    end 
--print("work_shedule!")
    _G.modFL = "0"
collectgarbage()
---------------------------
