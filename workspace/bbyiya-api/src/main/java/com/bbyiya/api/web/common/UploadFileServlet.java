package com.bbyiya.api.web.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.bbyiya.common.enums.UploadTypeEnum;
import com.bbyiya.common.vo.FileUploadParam;
import com.bbyiya.common.vo.ImageInfo;
import com.bbyiya.enums.ReturnStatus;
import com.bbyiya.utils.ConfigUtil;
import com.bbyiya.utils.JsonUtil;
import com.bbyiya.utils.ObjectUtil;
import com.bbyiya.utils.upload.FileUploadUtils_qiniu;
import com.bbyiya.vo.ReturnModel;
import com.bbyiya.vo.user.LoginSuccessResult;
import com.bbyiya.web.base.SSOController;
import com.sdicons.json.mapper.MapperException;

@WebServlet("/common/uploadfile")
public class UploadFileServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadFileServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ReturnModel rq = new ReturnModel();
		LoginSuccessResult userResult = SSOController.getLoginUser(request);
		if (userResult != null) {
//			rq = upload(request);
			try {
				FileUploadParam param = upload2(request);
				if(param!=null&&ObjectUtil.isEmpty(param.getFileTempPath())){
					if(param.getFileType()==Integer.parseInt(UploadTypeEnum.Product.toString())){
						String imgurl = FileUploadUtils_qiniu.uploadReturnUrl(param.getFileTempPath(), UploadTypeEnum.Product);
						if (!ObjectUtil.isEmpty(imgurl)) {
							ImageInfo img = new ImageInfo();
							img.setUrl(imgurl);
							ImageInfo imgSmall = new ImageInfo();
							imgSmall.setUrl(imgurl + "?imageView2/2/h/240/h/240");
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("small", imgSmall);
							map.put("original", img);
							rq.setStatu(ReturnStatus.Success);
							rq.setBasemodle(map);
						} else {
							rq.setStatu(ReturnStatus.SystemError);
							rq.setStatusreson("图片上传失败");
						}
					}else if (param.getFileType()==Integer.parseInt(UploadTypeEnum.Mp3.toString())) {//音乐上传
						String mp3Url = FileUploadUtils_qiniu.uploadReturnUrl(param.getFileTempPath(), UploadTypeEnum.Mp3);
						if (!ObjectUtil.isEmpty(mp3Url)) {
							rq.setStatu(ReturnStatus.Success);
							rq.setBasemodle(mp3Url);
						}
					}
					//删除临时文件
					File file = new File(param.getFileTempPath());
					if (file.isFile() && file.exists()) {
						file.delete();
						rq.setStatusreson("上传成功！");
					} else {
						rq.setStatu(ReturnStatus.SystemError);
						rq.setStatusreson("系统错误c01");
					}
				}else {
					rq.setStatu(ReturnStatus.SystemError);
					rq.setStatusreson("参数不全");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				rq.setStatu(ReturnStatus.SystemError);
				rq.setStatusreson(e.getMessage());
			}
			
		} else {
			rq.setStatu(ReturnStatus.SystemError);
			rq.setStatusreson("没有权限");
		}
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		try {
			out.println(JsonUtil.objectToJsonStr(rq));
		} catch (MapperException e) {
			e.printStackTrace();
		}
		out.flush();
		out.close();
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * 文件上传处理
	 * 
	 * @param request
	 * @return
	 */
	private ReturnModel upload(HttpServletRequest request) {
		ReturnModel rq = new ReturnModel();
		// 临时文件夹地址
		String savePath = System.getProperty("user.dir") + "/" + ConfigUtil.getSingleValue("imgPathTemp");
		File file = new File(savePath);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
		}

		try {
			DiskFileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("UTF-8");
			if (!ServletFileUpload.isMultipartContent(request)) {
				return rq;
			}
			List<FileItem> list = upload.parseRequest(request);
			FileUploadParam param = new FileUploadParam();
			for (FileItem item : list) {
				// 如果fileitem中封装的是普通输入项的数据
				if (item.isFormField()) {// 参数
					String name = item.getFieldName();
					// 解决普通输入项的数据的中文乱码问题
					String value = item.getString("UTF-8");
					// System.out.println(name+":"+value);
					if (name.equals("type")) {
						param.setFileType(ObjectUtil.parseInt(value));
					}
				} else {// 如果fileitem中封装的是上传文件
					// 得到上传的文件名称，
					String filename = item.getName();
					System.out.println("image:" + filename);
					if (filename == null || filename.trim().equals("")) {
						continue;
					}
					// 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如：
					// c:\a\b\1.txt，而有些只是单纯的文件名
					// 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
					filename = filename.substring(filename.lastIndexOf("\\") + 1);
					// 获取item中的上传文件的输入流
					InputStream in = item.getInputStream();
					// 创建一个文件输出流
					FileOutputStream out = new FileOutputStream(savePath + "/" + filename);
					// 创建一个缓冲区
					byte buffer[] = new byte[1024];
					// 判断输入流中的数据是否已经读完的标识
					int len = 0;
					// 循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
					while ((len = in.read(buffer)) > 0) {
						// 使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath + "\\"
						// + filename)当中
						out.write(buffer, 0, len);
					}
					// 关闭输入流
					in.close();
					// 关闭输出流
					out.close();
					// 删除处理文件上传时生成的临时文件
					item.delete();
					rq.setStatu(ReturnStatus.Success);
					param.setFileTempPath(savePath + "/" + filename);
					rq.setStatusreson(savePath + "/" + filename);
				}
			}
			rq.setBasemodle(param);
		} catch (Exception e) {
			rq.setStatu(ReturnStatus.SystemError);
			rq.setStatusreson("上传失败");
			rq.setBasemodle(e);
		}
		return rq;
	}

	private FileUploadParam upload2(HttpServletRequest request) throws Exception {
		// ReturnModel rq = new ReturnModel();
		FileUploadParam param = new FileUploadParam();
		// 临时文件夹地址
		String savePath = System.getProperty("user.dir") + "/" + ConfigUtil.getSingleValue("imgPathTemp");
		File file = new File(savePath);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdir();
		}

		DiskFileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding("UTF-8");
		if (!ServletFileUpload.isMultipartContent(request)) {
			throw new Exception("提交方式不对！");
		}
		List<FileItem> list = upload.parseRequest(request);
		for (FileItem item : list) {
			// 如果fileitem中封装的是普通输入项的数据
			if (item.isFormField()) {// 参数
				String name = item.getFieldName();
				// 解决普通输入项的数据的中文乱码问题
				String value = item.getString("UTF-8");
				// System.out.println(name+":"+value);
				if (name.equals("type")) {
					param.setFileType(ObjectUtil.parseInt(value));
				}
			} else {// 如果fileitem中封装的是上传文件
				// 得到上传的文件名称，
				String filename = item.getName();
				System.out.println("image:" + filename);
				if (filename == null || filename.trim().equals("")) {
					continue;
				}
				// 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的，如：
				// c:\a\b\1.txt，而有些只是单纯的文件名
				// 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
				filename = filename.substring(filename.lastIndexOf("\\") + 1);
				// 获取item中的上传文件的输入流
				InputStream in = item.getInputStream();
				// 创建一个文件输出流
				FileOutputStream out = new FileOutputStream(savePath + "/" + filename);
				// 创建一个缓冲区
				byte buffer[] = new byte[1024];
				// 判断输入流中的数据是否已经读完的标识
				int len = 0;
				// 循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
				while ((len = in.read(buffer)) > 0) {
					// 使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath + "\\"
					// + filename)当中
					out.write(buffer, 0, len);
				}
				// 关闭输入流
				in.close();
				// 关闭输出流
				out.close();
				// 删除处理文件上传时生成的临时文件
				item.delete();
				// rq.setStatu(ReturnStatus.Success);
				param.setFileTempPath(savePath + "/" + filename);
				// rq.setStatusreson(savePath + "/" + filename);
			}
		}
		// rq.setBasemodle(param);

		return param;
	}

}
