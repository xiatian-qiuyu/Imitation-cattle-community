package com.nowcoder.community.util;

import javax.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 过滤敏感词的工具类
 */
@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    private static final String REPLACEMENT  = "***";

    //根节点
    private final TrieNode rootNode = new TrieNode();


    //初始化
    @PostConstruct
    public void init(){
        //初始化前缀树
        try (
                //字节流
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                //将字节流转为字符流
                //字符流转为缓冲流
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        )
        {
            String keyword;
            while((keyword = bufferedReader.readLine())!=null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败："+e.getMessage());
        }

    }

    /**
     * 初始化前缀树
     * 将敏感词添加到前缀树
     * @param keyword
     */
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for(int i = 0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = rootNode.getSubNode(c);
            if(subNode==null){
                //初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }
            //指向子节点节点，进入下一循环
            tempNode = subNode;
            //结束标识
            if(i == keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 过滤敏感词
     * @param text 过滤前的文本
     * @return 过滤后的文本
     */
    public String filter(String text){
        if(StringUtils.isEmpty(text)){
            return null;
        }
        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuffer sb =new StringBuffer();
        while(begin<text.length()){
            if(position<text.length()){
                Character c = text.charAt(position);

                //跳过特殊符
                if(isSymbel(c)){
                    if(tempNode==rootNode){
                        //如果是开头为特殊符，则保留
                        begin++;
                        sb.append(c);
                    }
                    // 无论符号在开头或中间,指针3都向下走一步
                    position++;
                    continue;
                }
                //检查下级节点
                tempNode = tempNode.getSubNode(c);
                if(tempNode==null){
                    //以begin开头的字符不是敏感词
                    sb.append(text.charAt(begin));
                    //进入下一个位置
                    position=++begin;
                    //重新指向根节点
                    tempNode =rootNode;
                }else if(tempNode.isKeywordEnd()){
                    //发现敏感词,将begin到position的字符串替换掉
                    sb.append(REPLACEMENT);
                    //进入下一个位置
                    begin=++position;
                    //重新指向根结点
                    tempNode = rootNode;
                }else {
                    //检查下一个字符
                    position++;
                }

            }
        }
        //指针3到最后，指针2没走完
        sb.append(text.substring(begin));
        return sb.toString();
    }

    /**
     * 判断是否是特殊符
     * @param c
     * @return
     */
    private boolean isSymbel(Character c){
        //0x2E80-ox9FFF是东南亚文字范围
        //CharUtils.isAsciiAlphanumeric(c)是判断是否是普通字符
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }


    //前缀树的数据结构
    private class TrieNode{
        //1.关键词结束表示
        private boolean isKeywordEnd = false;
        //2.字节点
        Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }
        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }
        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
