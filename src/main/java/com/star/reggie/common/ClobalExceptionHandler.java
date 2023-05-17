package com.star.reggie.common;

import com.star.reggie.common.exception.DeleteIdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class ClobalExceptionHandler {
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHanlder(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        if(ex.getMessage().contains("Duplicate entry")){
            String[] split = ex.getMessage().split(" ");
            String msg = split[2]+"已存在";
            return R.error(msg);
        }

        return R.error("失败了");
    }

    @ExceptionHandler(DeleteIdException.class)
    public R<String> exceptionHanlder(DeleteIdException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
