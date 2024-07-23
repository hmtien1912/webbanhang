package com.vanlang.webbanhang.controller;

import com.vanlang.webbanhang.model.Product;
import com.vanlang.webbanhang.service.CategoryService;
import com.vanlang.webbanhang.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    private static String UPLOADED_FOLDER = "src/main/resources/static/images/";

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String showProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) Long categoryId,
            Model model) {
        List<Product> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = productService.searchProducts(keyword);
        } else if (categoryId != null) {
            products = productService.getProductsByCategory(categoryId);
        } else {
            products = productService.getAllProducts();
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        return "/products/products-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@Valid Product product, BindingResult bindingResult, @RequestParam("file") MultipartFile file) {
        if (bindingResult.hasErrors()) {
            return "products/add-product";
        }

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
                Files.write(path, bytes);
                product.setImage(file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        productService.addProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/update-product";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable("id") Long id, @Valid Product product, BindingResult bindingResult, @RequestParam("file") MultipartFile file) {
        if (bindingResult.hasErrors()) {
            product.setId(id); // keep the id in the form in case of errors
            return "/products/update-product";
        }

        Product existingProduct = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
                Files.write(path, bytes);
                product.setImage(file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Use the existing image if no new file is provided
            product.setImage(existingProduct.getImage());
        }

        productService.updateProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id) {
        // Retrieve the product to get its image name
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        // Delete the product from the database
        productService.deleteProductById(id);

        // Delete the product image file if it exists
        String imageFileName = product.getImage();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            try {
                Path imagePath = Paths.get(UPLOADED_FOLDER + imageFileName);
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                e.printStackTrace(); // Log the exception
            }
        }

        return "redirect:/products";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        List<Product> products = productService.searchProducts(keyword);
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "products/products-list";
    }

}
