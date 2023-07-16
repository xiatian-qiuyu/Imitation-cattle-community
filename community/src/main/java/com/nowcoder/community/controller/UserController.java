package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nowcoder.community.util.CommunityConstant.ENTITYTYPE_POST;
import static com.nowcoder.community.util.CommunityConstant.ENTITYTYPE_USER;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private CommentService commentService;

    //七牛云密钥
    @Value("${qiniu.key.access}")
    private String accessKey;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    //七牛云存储空间名
    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;
    //七牛云存储空间的域名
    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    /**
     * 跳转到个人主页
     * @param model
     * @return
     */
    //上传头像到七牛云，这里使用前端上传，后端只是生成token和存储七牛云返回的hash的任务。
    @LoginRequired
    @GetMapping("/setting")
    public String toUserSetting(Model model){
        //上传文件名xxx.png
        String fileName = CommunityUtil.gennerateUUID();
        //设置响应信息
        //这个policy是七牛云的上传策略，这里设置了上传成功后返回的信息
        StringMap policy = new StringMap();
        //returnBody是七牛云存储 API 中使用的固定密钥，是七牛云上传成功后返回的信息。
        //returnBody这个key是固定的，后面的值是自定义的。
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "/site/setting";
    }


    /**
     * 更新头像路径：七牛云
     */
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeader(String fileName){
        if(StringUtils.isEmpty(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空！");
        }
        //http://rvo4iks1w.bkt.clouddn.com/xxx
        String url = headerBucketUrl+"/"+fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);
        return CommunityUtil.getJSONString(0);
    }

    /**废弃
     * 上传头像：本地上传
     */
    @LoginRequired
    @PostMapping("/upload")
    public String uploadImg(Model model, MultipartFile headerImage){
        if(headerImage==null){
            model.addAttribute("error","未选择图片");
            return "/site/setting";
        }
        //获取文件名
        //getOriginalFilename()方法获取上传文件的原始名称
        String filename = headerImage.getOriginalFilename();
        //服务器报错：headerImage exceeds its maximum permitted size of 1048576 bytes.
        //a:这是因为上传的文件大小超过了springboot默认的1M大小
        //b:解决办法：在application.properties中配置spring.servlet.multipart.max-file-size=10MB
        //获取文件后缀
        String suffxx = filename.substring(filename.lastIndexOf("."));
        String contentType = headerImage.getContentType();
        //判断文件格式是否正确
//        if("image/jpeg".equals(contentType) || "image/png".equals(contentType) || "image/jpg".equals(contentType)){
//            model.addAttribute("error","文件格式不正确");
//            return "/site/setting";
//        }
        if(StringUtils.isEmpty(suffxx)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        //生成文件的随机文件名(xxx.png)
         filename = CommunityUtil.gennerateUUID() + suffxx;
        //设置文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //将headerImage写到dest文件中去.存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败:"+e.getMessage());
            throw new RuntimeException(e);
        }
        //更新当前用户头像访问路径（web访问路径）,自定义。
        //http://localhost:8083/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain+contextPath+"/user/header"+"/"+filename;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    /**废弃
     * 获取头像：本地
     */
    @GetMapping("/header/{filename}")
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response){
        //服务器存放路径
        filename = uploadPath+"/"+filename;
        //文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        //响应图片,固定格式：”image/“+后缀
        response.setContentType("image/"+suffix);

        try (  //新写法，在（）内的会自动关闭
               FileInputStream fileInputStream = new FileInputStream(filename);
               ServletOutputStream outputStream = response.getOutputStream();
        ){
            //缓冲区
            byte[] buffer = new byte[1024];
            int i=0;
            //将文件写到缓冲区
            while((i=fileInputStream.read(buffer))!=-1){
                outputStream.write(buffer,0,i);
            }
        } catch (IOException e) {
            logger.error("获取头像失败："+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/updatePassword")
    public String updatePswword(String username,String oldPassword,String newPassword,Model model){
        User user = hostHolder.getUser();
        if (StringUtils.isEmpty(oldPassword)||StringUtils.isEmpty(newPassword)) {
           model.addAttribute("passwordMsg","未输入密码");
            return "/site/setting";
        }else{
            oldPassword = CommunityUtil.md5(oldPassword+user.getSalt());
            newPassword = CommunityUtil.md5(newPassword+user.getSalt());
            //原密码正确
            if(oldPassword.equals(user.getPassword())){
                userService.updatePassword(user.getId(),newPassword);
               return "redirect:/logout";
            }else{
                model.addAttribute("passwordMsg","原密码不正确");
                return "/site/setting";
            }
        }
    }

    /**
     * 个人主页
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getUserProfilePage(@PathVariable("userId") int userId,Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        //用户信息
        model.addAttribute("user", user);

        //用户收到的点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITYTYPE_USER);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITYTYPE_USER, userId);
        //关注状态(true:已关注)
        boolean followStatus = false;
        if (hostHolder.getUser() != null) {
            followStatus = followService.followStatus(hostHolder.getUser().getId(), ENTITYTYPE_USER, userId);
        }
        model.addAttribute("followeeCount", followeeCount);
        model.addAttribute("followerCount", followerCount);
        model.addAttribute("followStatus", followStatus);
        //用户收到的点赞数量
        model.addAttribute("likeCount", likeCount);

        return "/site/profile";
    }

    /**
     * 我的帖子
     */
    @GetMapping("/myPost/{userId}")
    public String getMyPosts(@PathVariable("userId")int userId,Page page,Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user",user);

        //分页信息
        page.setLimit(10);
        page.setPath("/user/myPost/"+userId);
        page.setRows(discussPostService.findDisscussPostRow(userId));

        //帖子列表
        List<DiscussPost> discussPostList= discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(),0);
        List<Map<String,Object>> discussVoList = new ArrayList<>();
        if(discussPostList!=null&&!discussPostList.isEmpty()){
            for(DiscussPost post:discussPostList){
                Map<String, Object> map = new HashMap<>();
                map.put("discussPost",post);
                map.put("likeCount",likeService.findEntityLikeCount(ENTITYTYPE_POST,post.getId()));
                discussVoList.add(map);
            }
        }
        model.addAttribute("discussPosts",discussVoList);
        return "/site/my-post";
    }

    /**
     * 我的回复
     */
    @GetMapping("/myReply/{userId}")
    public String getMyRoply(@PathVariable("userId") int userId,Page page,Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user",user);

        //分页详情
        page.setLimit(10);
        page.setPath("/user/myReply/"+userId);
        page.setRows(commentService.findUserCommentCount(userId));

        //回复列表
        List<Comment> commentList = commentService.findUserComments(userId,page.getOffset(), page.getLimit());
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList!=null&&!commentList.isEmpty()){
            for(Comment comment : commentList){
                Map<String, Object> map = new HashMap<>();
                map.put("comment",comment);
                //回复的帖子
                DiscussPost post = discussPostService.selectDiscussPostById(comment.getEntityId());
                map.put("discussPost",post);
                commentVoList.add(map);
                System.out.println("comment-->"+comment.getContent());
                System.out.println("discussPost.title--->"+post.getTitle());
            }
        }
        model.addAttribute("comments",commentVoList);
        System.out.println("commentVoList-->");
        System.out.println(commentVoList);
        return "/site/my-reply";
    }
}
