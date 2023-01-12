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

    path_ = "value______day.lua"
    _G.modFL = 10
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
    _G.modFL = "0"
--    print("work_day!")
-- print(string.format("%02d.%02d.%04d %02d:%02d:%02d   %02d", tm_cur["day"], tm_cur["mon"], tm_cur["year"], tm_cur["hour"], tm_cur["min"], tm_cur["sec"], tm_cur["wday"]))
collectgarbage()
---------------------------
