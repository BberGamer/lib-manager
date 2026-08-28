/*
 * Bộ kiểm thử độc lập cho quy tắc khoảng nửa mở của đặt trước và gia hạn sách.
 * Lớp thuộc tầng kiểm thử, xác minh thuật toán thuần mà không cần kết nối cơ sở dữ liệu.
 */
package service;

import java.time.LocalDate;
import java.util.List;
import model.BorrowRecord;
import service.ReservationService.SlotPeriod;

/**
 * Chạy các kịch bản biên về overlap, sức chứa, trạng thái reservation và điều kiện gia hạn.
 */
public final class ReservationSlotScenarios {

    /** Ngăn khởi tạo vì lớp chỉ chứa kịch bản kiểm thử tĩnh. */
    private ReservationSlotScenarios() {
    }

    /**
     * Chạy toàn bộ kịch bản và ném lỗi ngay khi một quy tắc không còn đúng.
     *
     * @param arguments tham số dòng lệnh không được sử dụng
     */
    public static void main(String[] arguments) {
        verifyReservationExamples();
        verifyFutureReservationConsumesFullPeriod();
        verifyRenewalExamples();
        verifyCopyCapacity();
        verifyImmediateAvailableCount();
        verifyReservationStatuses();
        verifyBaseRenewalEligibility();
    }

    /** Xác minh các mốc 28/8, 3/9, 4/9 và 6/9 trong yêu cầu nghiệp vụ. */
    private static void verifyReservationExamples() {
        LocalDate august28 = LocalDate.of(2026, 8, 28);
        LocalDate september4 = LocalDate.of(2026, 9, 4);
        assertCondition(ReservationService.hasAvailableCapacity(1,
                List.of(new SlotPeriod(LocalDate.of(2026, 9, 6),
                        LocalDate.of(2026, 9, 13))), august28, september4),
                "Reservation tương lai không được chặn khoảng sớm hơn không giao nhau.");
        assertCondition(!ReservationService.hasAvailableCapacity(1,
                List.of(new SlotPeriod(LocalDate.of(2026, 9, 3),
                        LocalDate.of(2026, 9, 10))), august28, september4),
                "Slot bắt đầu 3/9 phải chặn khoảng kết thúc 4/9.");
        assertCondition(ReservationService.hasAvailableCapacity(1,
                List.of(new SlotPeriod(september4, LocalDate.of(2026, 9, 11))),
                august28, september4),
                "Slot bắt đầu đúng ngày kết thúc phải được phép.");
    }

    /** Xác minh reservation ngày 6/9 làm giảm capacity trong toàn khoảng [6/9, 13/9). */
    private static void verifyFutureReservationConsumesFullPeriod() {
        LocalDate september6 = LocalDate.of(2026, 9, 6);
        LocalDate september13 = september6.plusDays(BorrowService.LOAN_PERIOD_DAYS);
        SlotPeriod allocatedReservation = new SlotPeriod(september6, september13);
        assertCondition(ReservationService.calculateAvailableCapacity(1,
                List.of(allocatedReservation), september6, september13) == 0,
                "Reservation cùng ngày 6/9 phải dùng hết available của một bản đến 13/9.");
        assertCondition(ReservationService.calculateAvailableCapacity(2,
                List.of(allocatedReservation), september6, september13) == 1,
                "Một reservation ngày 6/9 chỉ được trừ một trong hai bản.");
        assertCondition(ReservationService.calculateAvailableCapacity(1,
                List.of(allocatedReservation), september13,
                september13.plusDays(BorrowService.LOAN_PERIOD_DAYS)) == 1,
                "Ngày 13/9 phải nhận lại capacity vì là biên cuối không thuộc slot trước.");
    }

    /** Xác minh overlap của khoảng gia hạn [4/9, 11/9). */
    private static void verifyRenewalExamples() {
        LocalDate currentDueDate = LocalDate.of(2026, 9, 4);
        LocalDate proposedDueDate = LocalDate.of(2026, 9, 11);
        assertCondition(!ReservationService.hasAvailableCapacity(1,
                List.of(new SlotPeriod(LocalDate.of(2026, 9, 6),
                        LocalDate.of(2026, 9, 13))), currentDueDate, proposedDueDate),
                "Reservation ngày 6/9 phải chặn gia hạn khi chỉ có một bản sao.");
        assertCondition(ReservationService.hasAvailableCapacity(1,
                List.of(new SlotPeriod(LocalDate.of(2026, 9, 12),
                        LocalDate.of(2026, 9, 19))), currentDueDate, proposedDueDate),
                "Reservation ngày 12/9 không được chặn gia hạn.");
        assertCondition(ReservationService.hasAvailableCapacity(1,
                List.of(new SlotPeriod(proposedDueDate,
                        proposedDueDate.plusDays(BorrowService.LOAN_PERIOD_DAYS))),
                currentDueDate, proposedDueDate),
                "Reservation bắt đầu đúng hạn mới 11/9 phải được phép.");
    }

    /** Xác minh một reservation không chặn toàn đầu sách khi vẫn còn bản sao khác. */
    private static void verifyCopyCapacity() {
        LocalDate startDate = LocalDate.of(2026, 9, 4);
        LocalDate endDate = LocalDate.of(2026, 9, 11);
        SlotPeriod overlap = new SlotPeriod(LocalDate.of(2026, 9, 6),
                LocalDate.of(2026, 9, 13));
        assertCondition(ReservationService.hasAvailableCapacity(2,
                List.of(overlap), startDate, endDate),
                "Một trong hai bản sao còn trống phải cho phép đặt hoặc gia hạn.");
        assertCondition(!ReservationService.hasAvailableCapacity(2,
                List.of(overlap, overlap), startDate, endDate),
                "Hai slot trùng nhau phải dùng hết sức chứa của hai bản sao.");
        assertCondition(!ReservationService.hasAvailableCapacity(1,
                List.of(new SlotPeriod(startDate, null)), startDate, endDate),
                "Bản sao quá hạn chưa biết ngày trả phải chặn slot tương ứng.");
    }

    /** Xác minh available phản ánh sức chứa cao nhất trong bảy ngày thay vì trừ lặp các slot nối tiếp. */
    private static void verifyImmediateAvailableCount() {
        LocalDate today = LocalDate.of(2026, 8, 28);
        LocalDate endDate = today.plusDays(BorrowService.LOAN_PERIOD_DAYS);
        int availableWithSequentialUsage = ReservationService.calculateAvailableCapacity(2,
                List.of(new SlotPeriod(today, today.plusDays(1)),
                        new SlotPeriod(today.plusDays(3), today.plusDays(10))),
                today, endDate);
        assertCondition(availableWithSequentialUsage == 1,
                "Hai slot không đồng thời chỉ được trừ một bản khỏi available.");
        int availableAtEndBoundary = ReservationService.calculateAvailableCapacity(2,
                List.of(new SlotPeriod(endDate,
                        endDate.plusDays(BorrowService.LOAN_PERIOD_DAYS))),
                today, endDate);
        assertCondition(availableAtEndBoundary == 2,
                "Reservation bắt đầu đúng ngày thứ bảy chưa được trừ available hôm nay.");
        int availableWithTwoOverlaps = ReservationService.calculateAvailableCapacity(2,
                List.of(new SlotPeriod(today.plusDays(1), today.plusDays(8)),
                        new SlotPeriod(today.plusDays(2), today.plusDays(9))),
                today, endDate);
        assertCondition(availableWithTwoOverlaps == 0,
                "Hai reservation giao nhau phải dùng hết available của hai bản sao.");
    }

    /** Xác minh chỉ WAITING và READY_FOR_PICKUP được tính là reservation hoạt động. */
    private static void verifyReservationStatuses() {
        assertCondition(ReservationService.isActiveReservationStatus("WAITING"),
                "WAITING phải chiếm slot.");
        assertCondition(ReservationService.isActiveReservationStatus("READY_FOR_PICKUP"),
                "READY_FOR_PICKUP phải chiếm slot.");
        for (String status : List.of("CANCELLED", "EXPIRED", "COMPLETED")) {
            assertCondition(!ReservationService.isActiveReservationStatus(status),
                    status + " không được chiếm slot.");
        }
    }

    /** Xác minh lượt hợp lệ, quá hạn và vượt số lần gia hạn. */
    private static void verifyBaseRenewalEligibility() {
        LocalDate today = LocalDate.of(2026, 9, 4);
        BorrowRecord record = new BorrowRecord();
        record.setStatus("BORROWED");
        record.setDueDate(today);
        record.setRenewalCount(BorrowService.MAXIMUM_RENEWALS - 1);
        assertCondition(BorrowService.isBaseRenewalEligible(record, today),
                "Lượt đúng hạn và còn số lần phải được xét gia hạn.");
        record.setDueDate(today.minusDays(1));
        assertCondition(!BorrowService.isBaseRenewalEligible(record, today),
                "Lượt quá hạn không được gia hạn.");
        record.setDueDate(today);
        record.setRenewalCount(BorrowService.MAXIMUM_RENEWALS);
        assertCondition(!BorrowService.isBaseRenewalEligible(record, today),
                "Lượt đạt số lần tối đa không được gia hạn.");
    }

    /**
     * Dừng bộ kiểm thử với thông báo nghiệp vụ khi điều kiện không đúng.
     *
     * @param condition kết quả cần đúng
     * @param message mô tả kịch bản thất bại
     */
    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
