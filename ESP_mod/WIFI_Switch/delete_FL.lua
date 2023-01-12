function del_fl(name_fl)        -- удаляем содрежимое файла
--  patch_ = name_fl..".log"

  if file.open(name_fl, "w+") ~= nil then            -- если файл существует
        str = ""
        file.write(str)
        file.close()
  end

end 

del_fl(_G.pathFL)
del_fl = nil
_G.modFL = "0" 
collectgarbage()
