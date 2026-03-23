package com.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/secure")
public class SecureAction {
	@GetMapping({"", "/index"})
	public String secureIndex() {
		return "secure/index";
	}
}

