package com.iot.learnssm.firstssm.controller;

import com.iot.learnssm.firstssm.controller.converter.validation.ValidGroup1;
import com.iot.learnssm.firstssm.po.ItemsCustom;
import com.iot.learnssm.firstssm.po.ItemsQueryVo;
import com.iot.learnssm.firstssm.service.ItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by brian on 2016/3/2.
 */

//使用@Controller来标识它是一个控制器
@Controller
//为了对url进行分类管理 ，可以在这里定义根路径，最终访问url是根路径+子路径
//比如：商品列表：/items/queryItems.action
@RequestMapping("/items")
public class ItemsController {

    @Autowired
    private ItemsService itemsService;

    // 商品分类
    //itemtypes表示最终将方法返回值放在request中的key
    @ModelAttribute("itemtypes")
    public Map<String, String> getItemTypes() {

        Map<String, String> itemTypes = new HashMap<String, String>();
        itemTypes.put("101", "数码");
        itemTypes.put("102", "母婴");

        return itemTypes;
    }

    //商品查询列表
    @RequestMapping("/queryItems")
    //实现 对queryItems方法和url进行映射，一个方法对应一个url
    //一般建议将url和方法写成一样
    public ModelAndView queryItems(HttpServletRequest request, ItemsQueryVo itemsQueryVo) throws Exception {
        //测试forward后request是否可以共享
        //System.out.println(request.getParameter("id"));

        //调用service查找数据库，查询商品列表
        List<ItemsCustom> itemsList = itemsService.findItemsList(itemsQueryVo);

        //返回ModelAndView
        ModelAndView modelAndView = new ModelAndView();
        //相当于request的setAttribute方法,在jsp页面中通过itemsList取数据
        modelAndView.addObject("itemsList", itemsList);

        //指定视图
        //下边的路径，如果在视图解析器中配置jsp的路径前缀和后缀，修改为items/itemsList
        //modelAndView.setViewName("/WEB-INF/jsp/items/itemsList.jsp");
        //下边的路径配置就可以不在程序中指定jsp路径的前缀和后缀
        modelAndView.setViewName("items/itemsList");

        return modelAndView;
    }

    //商品信息修改页面显示
    //@RequestMapping("/editItems")
    //限制http请求方法，可以post和get
  /*  @RequestMapping(value="/editItems",method={RequestMethod.POST, RequestMethod.GET})
    public ModelAndView editItems()throws Exception {

		//调用service根据商品id查询商品信息
		ItemsCustom itemsCustom = itemsService.findItemsById(1);

		// 返回ModelAndView
		ModelAndView modelAndView = new ModelAndView();

		//将商品信息放到model
		modelAndView.addObject("itemsCustom", itemsCustom);

		//商品修改页面
		modelAndView.setViewName("items/editItems");

		return modelAndView;
    }*/

    @RequestMapping(value = "/editItems", method = {RequestMethod.POST, RequestMethod.GET})
    //@RequestParam里边指定request传入参数名称和形参进行绑定。
    //通过required属性指定参数是否必须要传入
    //通过defaultValue可以设置默认值，如果id参数没有传入，将默认值和形参绑定。
    public String editItems(Model model, @RequestParam(value = "id", required = true) Integer items_id) throws Exception {

        //调用service根据商品id查询商品信息
        ItemsCustom itemsCustom = itemsService.findItemsById(items_id);

        //判断商品是否为空，根据id没有查询到商品，抛出异常，提示用户商品信息不存在
        //if(itemsCustom == null){
        //throw new CustomException("修改的商品信息不存在!");
        //}

        //通过形参中的model将model数据传到页面
        //相当于modelAndView.addObject方法
        model.addAttribute("items", itemsCustom);

        return "items/editItems";
    }

    //查询商品信息，输出json
    //itemsView/{id}里边的{id}表示占位符，通过@PathVariable获取占位符中的参数，
    //@PathVariable中名称要和占位符一致，形参名无需和其一致
    //如果占位符中的名称和形参名一致，在@PathVariable可以不指定名称
    @RequestMapping("/itemsView/{id}")
    public
    @ResponseBody
    ItemsCustom itemsView(@PathVariable("id") Integer items_id) throws Exception {

        //调用service查询商品信息
        ItemsCustom itemsCustom = itemsService.findItemsById(items_id);

        return itemsCustom;

    }

    // 商品信息修改提交
    // 在需要校验的pojo前边添加@Validated，在需要校验的pojo后边添加BindingResult
    // bindingResult接收校验出错信息
    // 注意：@Validated和BindingResult bindingResult是配对出现，并且形参顺序是固定的（一前一后）。
    // value={ValidGroup1.class}指定使用ValidGroup1分组的校验
    @RequestMapping("/editItemsSubmit")
    public String editItemsSubmit(
            Model model,
            HttpServletRequest request,
            Integer id,
            @ModelAttribute("items")
            @Validated(value = ValidGroup1.class) ItemsCustom itemsCustom,
            BindingResult bindingResult,
            MultipartFile items_pic
    ) throws Exception {

        //获取校验错误信息
        if (bindingResult.hasErrors()) {
            // 输出错误信息
            List<ObjectError> allErrors = bindingResult.getAllErrors();

            for (ObjectError objectError : allErrors) {
                // 输出错误信息
                System.out.println(objectError.getDefaultMessage());
            }
            // 将错误信息传到页面
            model.addAttribute("allErrors", allErrors);

            //可以直接使用model将提交pojo回显到页面
            //model.addAttribute("items", itemsCustom);

            // 出错重新到商品修改页面
            return "items/editItems";
        }

        //原始名称
        String originalFilename = items_pic.getOriginalFilename();
        //上传图片
        if (items_pic != null && originalFilename != null && originalFilename.length() > 0) {

            //存储图片的物理路径
            String pic_path = "D:\\tmp\\";

            //新的图片名称
            String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
            //新图片
            File newFile = new File(pic_path + newFileName);

            //将内存中的数据写入磁盘
            items_pic.transferTo(newFile);

            //将新图片名称写到itemsCustom中
            itemsCustom.setPic(newFileName);

        }

        //调用service更新商品信息，页面需要将商品信息传到此方法
        itemsService.updateItems(id, itemsCustom);

        //重定向到商品查询列表
        //return "redirect:queryItems.action";
        //页面转发
        return "forward:queryItems.action";

    }

    //商品信息修改提交
    //@RequestMapping("/editItemsSubmit")
    //public ModelAndView editItemsSubmit(HttpServletRequest request, Integer id, ItemsCustom itemsCustom)throws Exception {
    //
    //    //调用service更新商品信息，页面需要将商品信息传到此方法
    //    itemsService.updateItems(id, itemsCustom);
    //
    //    //返回ModelAndView
    //    ModelAndView modelAndView = new ModelAndView();
    //    //返回一个成功页面
    //    modelAndView.setViewName("success");
    //    return modelAndView;
    //}

    ////商品信息修改提交
    //@RequestMapping("/success")
    //public ModelAndView success(){
    //    //返回ModelAndView
    //    ModelAndView modelAndView = new ModelAndView();
    //    //返回一个成功页面
    //    modelAndView.setViewName("success");
    //    return modelAndView;
    //}

    // 批量删除 商品信息
    @RequestMapping("/deleteItems")
    public String deleteItems(Integer[] items_id) throws Exception {

        // 调用service批量删除商品
        // ...
        System.out.println(items_id);

        return "success";

    }

    // 批量修改商品页面，将商品信息查询出来，在页面中可以编辑商品信息
    @RequestMapping("/editItemsQuery")
    public ModelAndView editItemsQuery(HttpServletRequest request, ItemsQueryVo itemsQueryVo) throws Exception {

        //调用service查找数据库，查询商品列表
        List<ItemsCustom> itemsList = itemsService.findItemsList(itemsQueryVo);

        //返回ModelAndView
        ModelAndView modelAndView = new ModelAndView();
        //相当于request的setAttribute方法,在jsp页面中通过itemsList取数据
        modelAndView.addObject("itemsList", itemsList);

        modelAndView.setViewName("items/editItemsQuery");

        return modelAndView;
    }

    // 批量修改商品提交
    // 通过ItemsQueryVo接收批量提交的商品信息，将商品信息存储到itemsQueryVo中itemsList属性中。
    @RequestMapping("/editItemsAllSubmit")
    public String editItemsAllSubmit(ItemsQueryVo itemsQueryVo) throws Exception {

        return "success";
    }

}
