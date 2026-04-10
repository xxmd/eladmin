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
package me.zhengjie.service;

import me.zhengjie.domain.ApkSign;
import me.zhengjie.utils.ValidationUtil;
import me.zhengjie.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import me.zhengjie.repository.ApkSignRepository;
import me.zhengjie.service.ApkSignService;
import me.zhengjie.service.dto.ApkSignDto;
import me.zhengjie.service.dto.ApkSignQueryCriteria;
import me.zhengjie.service.mapstruct.ApkSignMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import me.zhengjie.utils.PageResult;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author gmj
* @date 2026-04-11
**/
@Service
@RequiredArgsConstructor
public class ApkSignService {

    private final ApkSignRepository apkSignRepository;
    private final ApkSignMapper apkSignMapper;

    public PageResult<ApkSignDto> queryAll(ApkSignQueryCriteria criteria, Pageable pageable){
        Page<ApkSign> page = apkSignRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(apkSignMapper::toDto));
    }

    public List<ApkSignDto> queryAll(ApkSignQueryCriteria criteria){
        return apkSignMapper.toDto(apkSignRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Transactional
    public ApkSignDto findById(Long signId) {
        ApkSign apkSign = apkSignRepository.findById(signId).orElseGet(ApkSign::new);
        ValidationUtil.isNull(apkSign.getSignId(),"ApkSign","signId",signId);
        return apkSignMapper.toDto(apkSign);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(ApkSign item) {
        apkSignRepository.save(item);
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void update(ApkSign item) {
        ApkSign apkSign = apkSignRepository.findById(item.getSignId()).orElseGet(ApkSign::new);
        ValidationUtil.isNull( apkSign.getSignId(),"ApkSign","id",item.getSignId());
        apkSign.copy(item);
        apkSignRepository.save(apkSign);
    }

    
    public void deleteAll(Long[] ids) {
        for (Long signId : ids) {
            apkSignRepository.deleteById(signId);
        }
    }

    
    public void download(List<ApkSignDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ApkSignDto apkSign : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("密钥库密码", apkSign.getStorePass());
            map.put("密钥别名", apkSign.getAlias());
            map.put("密钥密码", apkSign.getKeyPass());
            map.put("文件id", apkSign.getFileId());
            map.put("备注", apkSign.getRemark());
            map.put("创建者", apkSign.getCreateBy());
            map.put("创建日期", apkSign.getCreateTime());
            map.put("更新者", apkSign.getUpdateBy());
            map.put("更新时间", apkSign.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}