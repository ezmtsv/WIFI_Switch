local str_data = ""

str_data = string.format("%02d.%02d.%04d %02d:%02d:%02d", tm["day"], tm["mon"], tm["year"], tm["hour"], tm["min"], tm["sec"])
local str_com = string.char(52)           -- команда 52 переводится в символьную форму
str_data = str_com..str_data.."!"
if _G.stateON == 1 then
	str_data = str_data.."1!"
	else
	str_data = str_data.."0!"
end	
str_data = str_data.._G.mode_work.."!"
print(str_data)

pcall(function () sock_serv:send(_G.init_word.."8888_"..str_data) end)   -- отправка ответа клиенту

_G.modFL = "0" 
collectgarbage()
