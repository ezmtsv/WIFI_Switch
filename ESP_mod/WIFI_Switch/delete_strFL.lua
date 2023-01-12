function str_del(name_fl, data)   -- удаление строки data из файла name_fl с уменьшением в первой строке кол-ва записей
--  patch_ = name_fl..".log"
 
 if file.open(name_fl, "r") ~= nil then            -- если файл существует
    numstr = file.readline()
    if numstr~= nil then                            -- если файл существует и он не пустой

        str = ""
        num_eq = 0
        for i=2,numstr+1 do
          strtmp = file.readline()
--        print("strtmp "..strtmp..", cnt symb= "..#strtmp.."\n")  
          if rawequal(strtmp, data.."\n") == false then
            str = str..strtmp
            else num_eq = num_eq + 1
          end  
--          print("i= "..i..", str= "..str)
        end
        numstr = numstr - num_eq
        str = numstr.."\n"..str                     -- формируем новый контекст для записи
        print(" str \n"..str)
        if numstr == 0 then str = "" end
        file.open(name_fl, "w+")  
        file.write(str)
        file.close()

    end    
  end  

end

str_del(_G.pathFL, _G.strFL)
str_del = nil
_G.modFL = "0" 
collectgarbage()
