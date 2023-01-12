
local path_ = _G.pathFL
-- local path_ = "value______day.lua"
local cnt_pos = 0		-- строк в файле
local strtmp
local str_data = ""
local cnt_pack = 0		-- символов в пакете
local cnt_str = 0		-- символов в строке

local sock_
    if file.open(path_, "r") ~= nil then
        cnt_pos = file.readline()           -- считываем кол-во записей
        if cnt_pos~= nil then
            for i = 1, cnt_pos do			-- цикл по всем строкам файла
                strtmp = file.readline()          -- считываем текущую запись
				cnt_str = #strtmp
				cnt_pack = cnt_pack + cnt_str
				str_data = str_data..strtmp
            end
        end
        file.close()
    end
     str_data = cnt_pack.."!"..str_data.."!"
     print("cnt_pack "..cnt_pack.." \n"..str_data)
	 pcall(function () sock_serv:send(_G.init_word.."8888_!"..str_data) end)   -- отправка ответа клиенту
	_G.modFL = "0" 
----------------------------

--------------------------
collectgarbage()
