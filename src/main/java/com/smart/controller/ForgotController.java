package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private EmailService emailService;
	Random random = new Random(1000);
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
	return "forgot_email_form";
	}
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email")String email,HttpSession session)
	{
		//generating 4 digit otp
		System.out.println(email);
		
		int otp = random.nextInt(999999);
		System.out.println("OTP: "+otp);
		//write code for send otp to email here
		String subject="OTP From SCM";
		String message="<div style='border:1px solid #e2e2e2;padding:20px;'>"+
				"<h1>"
				+"OTP is "
				+"<b>"+otp
				+"</b>"
				+"</n>"
				+"</h1>"
				+"</div>";
		String to=email;
		boolean flag = this.emailService.sendEmail(subject, message,to);
		if(flag)
		{
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}
		else
		{
			session.setAttribute("messsage","Check you email-id");
			return "forgot_email_form";
	}
}
	@PostMapping("/verify-otp")
	public String verifyOTP(@RequestParam("otp")int otp,HttpSession session) {
		int myotp=(int)session.getAttribute("myotp");
		String email=(String)session.getAttribute("email");
		if(myotp==otp) {
			User user = this.userRepository.getUserByUsername(email);
			if(user==null)
			{
				//send error message
				session.setAttribute("message","No user with this email");
				return "forgot_email_form";
			}
			else {
				//send change pasword form
			}
			return "password_change_form";
		}
		else
		{
			session.setAttribute("message","You have entered wrong otp");
			return "verify_otp";
		}
	}
	@PostMapping("/changePassword")
	public String changePassword(@RequestParam("newPassword")String newPassword,
			HttpSession session) {
	String email=(String)session.getAttribute("email");
	User user = this.userRepository.getUserByUsername(email);
	user.setPassword(bCryptPasswordEncoder.encode(newPassword));
	this.userRepository.save(user);
		return "redirect:/signin?change=Password Changed Successfully..";
	}
	
	
}
