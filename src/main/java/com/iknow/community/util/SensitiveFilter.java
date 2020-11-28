package com.iknow.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤器
 */
@Component
public class SensitiveFilter {

    private static Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static String REPLACEMENT = "***";

    // 初始化
    private TrieNode rootNode = new TrieNode();

    //初始化
    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null){
                // 添加到前缀树
                this.addKeyWord(keyword);
            }

        } catch (IOException e) {
            logger.error("敏感词加载失败:" + e.getMessage());
        }

    }

    private void addKeyWord(String keyword){
        TrieNode tempNode = rootNode;
        for (int i=0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null){
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            // 指向子节点，进入下一轮循环
            tempNode = subNode;
            // 设置结束标识符
            if (i==keyword.length()-1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();


        while (position < text.length()){
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)){
                // 若指针1处于根节点，将此节点计入结果，让指针2向下走一步
                if (tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都往下走一步
                position++;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null){
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position=++begin;
                // 重新指向根节点
                tempNode = rootNode;

            }else if (tempNode.isIskeyWordEnd()){
                // 发现敏感词，将begin-position字符串替换掉
                sb.append(REPLACEMENT);
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = rootNode;
            }else {
                // 检查下一个字符
                position++;
            }
        }
        // 将最后一批字符计入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c){
        // 0x2e80 - 0x9fff 是 东亚文字范围 例如 中文 韩文。。。
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2e80 || c > 0x9fff);
    }

    //定义前缀树结构
    private class TrieNode {
        // 关键词结束标识符
        private boolean iskeyWordEnd = false;

        // 子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isIskeyWordEnd() {
            return this.iskeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            this.iskeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode trieNode) {
            subNodes.put(c, trieNode);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }

}
