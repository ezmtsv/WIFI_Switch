---------------
function write_week(day_week, data)   -- при отсутствии файла day_week создаст его. Первой строкой
  -- пишет кол-во строк(записей) в файле с учетом добавленной записи, и добавляет строку data в конец файла
--  patch_ = day_week..".log"
  if file.open(day_week, "r") ~= nil then
    numstr = file.readline()		-- читаем 1 строку, в которой указано кол-во записей
    if numstr~= nil then
      str = file.read()
      numstr = numstr + 1
      str = numstr.."\n"..str
    else 
      str = "1".."\n"
    end  
  else
    str = "1".."\n"
  end  
--    print(numstr)
  file.open(day_week, "w+")  
  str = str..data.."\n"
--	print("на запись \n"..str)
--	print("end_wr")
  file.write(str)
--  str = file:read"*a"
  file.close()
end
write_week(_G.pathFL, _G.strFL)
write_week = nil
_G.modFL = "0" 
collectgarbage()
