package com.fashion_store.seeder;

import com.fashion_store.entity.Permission;
import com.fashion_store.entity.Role;
import com.fashion_store.entity.User;
import com.fashion_store.repository.PermissionRepository;
import com.fashion_store.repository.RoleRepository;
import com.fashion_store.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataSeeder implements ApplicationRunner {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (permissionRepository.count() == 0) {
            List<Permission> defaultPermissions = generateDefaultPermissions();
            permissionRepository.saveAll(defaultPermissions);
        }

        if (roleRepository.count() == 0) {
            List<Permission> allPermissions = permissionRepository.findAll();

            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .status(true)
                    .permissions(new HashSet<>(allPermissions))
                    .build();

            roleRepository.save(adminRole);
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName("ADMIN");
            User admin = User.builder()
                    .username("admin")
                    .status(true)
                    .role(adminRole)
                    .password(passwordEncoder.encode("123456789"))
                    .build();
            userRepository.save(admin);
            log.warn("User admin created with default password: 123456789. Please change it.");
        }
    }

    List<Permission> generateDefaultPermissions() {
        return List.of(
                Permission.builder().code("PRODUCT_VIEW").name("Xem").module("Sản phẩm").build(),
                Permission.builder().code("PRODUCT_CREATE").name("Thêm mới").module("Sản phẩm").build(),
                Permission.builder().code("PRODUCT_UPDATE").name("Cập nhật").module("Sản phẩm").build(),
                Permission.builder().code("PRODUCT_DELETE").name("Xóa").module("Sản phẩm").build(),

                Permission.builder().code("CATEGORY_VIEW").name("Xem").module("Danh mục").build(),
                Permission.builder().code("CATEGORY_CREATE").name("Thêm mới").module("Danh mục").build(),
                Permission.builder().code("CATEGORY_UPDATE").name("Cập nhật").module("Danh mục").build(),
                Permission.builder().code("CATEGORY_DELETE").name("Xóa").module("Danh mục").build(),

                Permission.builder().code("CUSTOMER_VIEW").name("Xem").module("Khách hàng").build(),
                Permission.builder().code("CUSTOMER_CREATE").name("Thêm mới").module("Khách hàng").build(),
                Permission.builder().code("CUSTOMER_UPDATE").name("Cập nhật").module("Khách hàng").build(),
                Permission.builder().code("CUSTOMER_DELETE").name("Xóa").module("Khách hàng").build(),

                Permission.builder().code("USER_VIEW").name("Xem").module("Người dùng").build(),
                Permission.builder().code("USER_CREATE").name("Thêm mới").module("Người dùng").build(),
                Permission.builder().code("USER_UPDATE").name("Cập nhật").module("Người dùng").build(),
                Permission.builder().code("USER_DELETE").name("Xóa").module("Người dùng").build(),

                Permission.builder().code("POST_VIEW").name("Xem").module("Bài viết").build(),
                Permission.builder().code("POST_CREATE").name("Thêm mới").module("Bài viết").build(),
                Permission.builder().code("POST_UPDATE").name("Cập nhật").module("Bài viết").build(),
                Permission.builder().code("POST_DELETE").name("Xóa").module("Bài viết").build(),

                Permission.builder().code("BRAND_VIEW").name("Xem").module("Thương hiệu").build(),
                Permission.builder().code("BRAND_CREATE").name("Thêm mới").module("Thương hiệu").build(),
                Permission.builder().code("BRAND_UPDATE").name("Cập nhật").module("Thương hiệu").build(),
                Permission.builder().code("BRAND_DELETE").name("Xóa").module("Thương hiệu").build(),

                Permission.builder().code("VOUCHER_VIEW").name("Xem").module("Voucher").build(),
                Permission.builder().code("VOUCHER_CREATE").name("Thêm mới").module("Voucher").build(),
                Permission.builder().code("VOUCHER_UPDATE").name("Cập nhật").module("Voucher").build(),
                Permission.builder().code("VOUCHER_DELETE").name("Xóa").module("Voucher").build(),

                Permission.builder().code("TOPIC_VIEW").name("Xem").module("Chủ đề").build(),
                Permission.builder().code("TOPIC_CREATE").name("Thêm mới").module("Chủ đề").build(),
                Permission.builder().code("TOPIC_UPDATE").name("Cập nhật").module("Chủ đề").build(),
                Permission.builder().code("TOPIC_DELETE").name("Xóa").module("Chủ đề").build(),

                Permission.builder().code("IMPORT_RECEPT_VIEW").name("Xem").module("Nhập hàng").build(),
                Permission.builder().code("IMPORT_RECEPT_CREATE").name("Thêm mới").module("Nhập hàng").build(),
                Permission.builder().code("IMPORT_RECEPT_UPDATE").name("Cập nhật").module("Nhập hàng").build(),
                Permission.builder().code("IMPORT_RECEPT_DELETE").name("Xóa").module("Nhập hàng").build(),

                Permission.builder().code("ROLE_VIEW").name("Xem").module("Vai trò").build(),
                Permission.builder().code("ROLE_CREATE").name("Thêm mới").module("Vai trò").build(),
                Permission.builder().code("ROLE_UPDATE").name("Cập nhật").module("Vai trò").build(),
                Permission.builder().code("ROLE_DELETE").name("Xóa").module("Vai trò").build(),
                Permission.builder().code("PERMISSION_UPDATE").name("Phân quyền").module("Vai trò").build(),

                Permission.builder().code("ATTRIBUTE_VIEW").name("Xem").module("Thuộc tính").build(),
                Permission.builder().code("ATTRIBUTE_CREATE").name("Thêm mới").module("Thuộc tính").build(),
                Permission.builder().code("ATTRIBUTE_UPDATE").name("Cập nhật").module("Thuộc tính").build(),
                Permission.builder().code("ATTRIBUTE_DELETE").name("Xóa").module("Thuộc tính").build(),

                Permission.builder().code("ATTRIBUTE_VALUE_VIEW").name("Xem").module("Giá trị thuộc tính").build(),
                Permission.builder().code("ATTRIBUTE_VALUE_CREATE").name("Thêm mới").module("Giá trị thuộc tính").build(),
                Permission.builder().code("ATTRIBUTE_VALUE_UPDATE").name("Cập nhật").module("Giá trị thuộc tính").build(),
                Permission.builder().code("ATTRIBUTE_VALUE_DELETE").name("Xóa").module("Giá trị thuộc tính").build(),

                Permission.builder().code("ORDER_VIEW").name("Xem").module("Đơn hàng").build(),
                Permission.builder().code("ORDER_CREATE").name("Thêm mới").module("Đơn hàng").build(),
                Permission.builder().code("ORDER_UPDATE").name("Cập nhật").module("Đơn hàng").build(),
                Permission.builder().code("ORDER_DELETE").name("Xóa").module("Đơn hàng").build(),

                Permission.builder().code("SUPPLIER_VIEW").name("Xem").module("Nhà cung cấp").build(),
                Permission.builder().code("SUPPLIER_CREATE").name("Thêm mới").module("Nhà cung cấp").build(),
                Permission.builder().code("SUPPLIER_UPDATE").name("Cập nhật").module("Nhà cung cấp").build(),
                Permission.builder().code("SUPPLIER_DELETE").name("Xóa").module("Nhà cung cấp").build()
        );
    }
}
