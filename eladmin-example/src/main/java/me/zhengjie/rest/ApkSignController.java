/*
*  Copyright 2019-2025 Zheng Jie
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package me.zhengjie.rest;

import me.zhengjie.annotation.Log;
import me.zhengjie.domain.ApkSign;
import me.zhengjie.service.ApkSignService;
import me.zhengjie.service.dto.ApkSignQueryCriteria;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import me.zhengjie.utils.PageResult;
import me.zhengjie.service.dto.ApkSignDto;

/**
* @website https://eladmin.vip
* @author gmj
* @date 2026-04-11
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "应用签名")
@RequestMapping("/api/apk/sign")
public class ApkSignController {

    private final ApkSignService apkSignService;

    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('apk:sign:list')")
    public void exportApkSign(HttpServletResponse response, ApkSignQueryCriteria criteria) throws IOException {
        apkSignService.download(apkSignService.queryAll(criteria), response);
    }

    @GetMapping
    @ApiOperation("查询应用签名")
    @PreAuthorize("@el.check('apk:sign:list')")
    public ResponseEntity<PageResult<ApkSignDto>> queryApkSign(ApkSignQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(apkSignService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增应用签名")
    @ApiOperation("新增应用签名")
    @PreAuthorize("@el.check('apk:sign:add')")
    public ResponseEntity<Object> createApkSign(@Validated @RequestBody ApkSign item){
        apkSignService.create(item);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改应用签名")
    @ApiOperation("修改应用签名")
    @PreAuthorize("@el.check('apk:sign:edit')")
    public ResponseEntity<Object> updateApkSign(@Validated @RequestBody ApkSign item){
        apkSignService.update(item);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除应用签名")
    @ApiOperation("删除应用签名")
    @PreAuthorize("@el.check('apk:sign:del')")
    public ResponseEntity<Object> deleteApkSign(@ApiParam(value = "传ID数组[]") @RequestBody Long[] ids) {
        apkSignService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}