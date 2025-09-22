package com.fashion_store.service;

import com.fashion_store.dto.voucher.request.VoucherRequest;
import com.fashion_store.dto.voucher.request.VoucherUpdateRequest;
import com.fashion_store.dto.voucher.response.VoucherResponse;
import com.fashion_store.entity.Voucher;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.VoucherMapper;
import com.fashion_store.repository.VoucherRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherService extends GenerateService<Voucher, Long> {
    VoucherRepository voucherRepository;
    VoucherMapper voucherMapper;

    @Override
    JpaRepository<Voucher, Long> getRepository() {
        return voucherRepository;
    }

    public VoucherResponse create(VoucherRequest request) {
        if (voucherRepository.existsByName(request.getName()))
            throw new AppException(ErrorCode.EXISTED);
        if (voucherRepository.existsByCode(request.getCode()))
            throw new AppException(ErrorCode.EXISTED);
        request.setDiscountType(request.getDiscountType().toUpperCase().trim());
        Voucher voucher = voucherMapper.toVoucher(request);

        voucher.setUsed(0);
        voucher = voucherRepository.save(voucher);
        return voucherMapper.toVoucherResponse(voucher);
    }

    public List<VoucherResponse> getAll(boolean deleted, String search) {
        return voucherRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == deleted)
                .filter(item -> {
                    if (search != null) {
                        return item.getName().trim().toLowerCase().contains(search.trim().toLowerCase()) ||
                                item.getCode().trim().equals(search.trim().toLowerCase());
                    } else {
                        return true;
                    }
                })
                .map(voucherMapper::toVoucherResponse)
                .collect(Collectors.toList());
    }

    public List<VoucherResponse> getVoucher() {
        return voucherRepository.findAll()
                .stream()
                .filter(item -> item.getIsDeleted() == false && item.getStatus() == true)
                .map(voucherMapper::toVoucherResponse)
                .collect(Collectors.toList());
    }

    public VoucherResponse getInfo(Long id) {
        Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        return voucherMapper.toVoucherResponse(voucher);
    }

    public VoucherResponse update(VoucherUpdateRequest request, Long id) {
        Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (voucherRepository.existsByNameAndIdNot(request.getName(), id))
            throw new AppException(ErrorCode.EXISTED);
        if (voucherRepository.existsByCodeAndIdNot(request.getName(), id))
            throw new AppException(ErrorCode.EXISTED);

        if (request.getDiscountType() != null) {
            request.setDiscountType(request.getDiscountType().toUpperCase().trim());
        }
        voucherMapper.updateVoucher(voucher, request);

        voucher = voucherRepository.save(voucher);
        return voucherMapper.toVoucherResponse(voucher);
    }

    public void status(Long id) {
        Voucher voucher = voucherRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        try {
            voucher.setStatus(voucher.getStatus() == null || !voucher.getStatus());
            voucherRepository.save(voucher);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INTERNAL_EXCEPTION);
        }
    }
}
