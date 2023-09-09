package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@ModelAttribute
	public void addComonData(Model model, Principal principal) {
		String username = principal.getName();
		User user = userRepository.getUserByUsername(username);
		//System.out.println(user);
		model.addAttribute("user",user);
		
		System.out.println(username);
	}
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title","User dashboard");
		return "normal/user_dashboard";
	}
	@RequestMapping("/addContact")
	public String addContactForm(Model model) 
	{	model.addAttribute("title","Add Contact");
	model.addAttribute("contact",new Contact());
		return "normal/add_contact";
	}
	@RequestMapping(value="/processContact",method=RequestMethod.POST)
	public String processContactForm(@ModelAttribute Contact contact
			,@RequestParam("profileImage")MultipartFile file
			,Principal principal,Model model,HttpSession session) 
	{
		
		
		
		try {
		String name = principal.getName();
		User user = userRepository.getUserByUsername(name);
		contact.setUser(user);
		user.getContacts().add(contact);
		//processing and uploading file
		if(file.isEmpty()) {
			contact.setImage("contact.png");
		}
		else {
			//upload the file and update the name inside contact
			contact.setImage(file.getOriginalFilename());
			 File file2 = new ClassPathResource("static/images").getFile();
			 Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
			 Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
		}
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Your contact is added","success"));
		//success message
		}
		catch(Exception e) {
			e.printStackTrace();
			//error message
			session.setAttribute("message", new Message("Something went wrong..Try Again!","danger"));	
		
		}
		
		return "normal/add_contact";
	}
	//show contacts handler
	
	@GetMapping("/viewContacts/{pageNo}")
	public String viewContacts(@PathVariable("pageNo") int pageNo,Model model,Principal principal)
	{	
		model.addAttribute("title","View Contacts");
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
	Pageable pageable = PageRequest.of(pageNo, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage",pageNo);
		model.addAttribute("TotalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	//showing specific contact detail
	@RequestMapping("{cid}/contact")
	public String showContactDetail(@PathVariable("cid")int cid,Principal principal,Model model) {
		System.out.println(cid);
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		Contact contact = this.contactRepository.findById(cid).get();
		if(user.getId()==contact.getUser().getId())
		{
		model.addAttribute("contact",contact);
		model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail";
	}
	@GetMapping("/deleteContact/{cid}")
	public String deleteContact(@PathVariable("cid")int cid,HttpSession session,Principal principal)
	{
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		Contact contact = this.contactRepository.findById(cid).get();
		
		if(user.getId()==contact.getUser().getId())
		{
		contact.setUser(null);
		this.contactRepository.delete(contact);
		session.setAttribute("message",new Message("Contact Deleted Successfully","success"));
		}
		return "redirect:/user/viewContacts/0";
	}
	//UPDATE FORM HANDLER
	@PostMapping("/updateContact/{cid}")
	public String updateContact(@PathVariable("cid")int cid,Model model) {
		model.addAttribute("title","Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_contact";
		
	}
	//PROCESS UPATE FORM HANDLER
	@RequestMapping(value="/processUpdate",method = RequestMethod.POST)
	public String processUpdateForm(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file
			,Principal principal
			,HttpSession sesssion,Model model)
	{
		try {
			Contact old_contact = this.contactRepository.findById(contact.getCid()).get();
			
			if(!file.isEmpty()) {
				
				//delete old photo
				File deleteFile=new ClassPathResource("static/images").getFile();
				File f1=new File(deleteFile,old_contact.getImage());
				f1.delete();
				
				//update new image
				contact.setImage(file.getOriginalFilename());
				File file2 = new ClassPathResource("static/images").getFile();
			Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING );
				
			}
			else
			{
				contact.setImage(old_contact.getImage());
			}
			User user = this.userRepository.getUserByUsername(principal.getName());
			contact.setUser(user);
			System.out.println(contact.getCid());
			this.contactRepository.save(contact);
			
			System.out.println(contact.getName());
		}
		catch(Exception e) {e.printStackTrace();}
		

		
		return "redirect:/user/"+contact.getCid()+"/contact";
		
	}
	
	//USER PROFILE HANDLER
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
	return "normal/profile";
	}
	
	//OPEN SETTINGS HANDLER
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	@PostMapping("/changePassword")
	public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword,Principal principal,
			HttpSession session) {
		String username = principal.getName();
		User user = this.userRepository.getUserByUsername(username);
		System.out.println(user.getPassword());
		if(this.bCryptPasswordEncoder.matches(oldPassword,user.getPassword()))
		{
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(user);
		session.setAttribute("message", new Message("Your password is changed","success"));
		}
		else {
			session.setAttribute("message", new Message("Please Enter correct old password","danger"));
		}
		
		return "redirect:/user/index";
	}
	
}
