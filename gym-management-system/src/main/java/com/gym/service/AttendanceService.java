package com.gym.service;

import com.gym.exception.ResourceNotFoundException;
import com.gym.model.Attendance;
import com.gym.model.Member;

import com.gym.repository.AttendanceRepository;
import com.gym.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Attendance service – extends {@link BaseCrudService} (Template Method pattern).
 */
@Service
@RequiredArgsConstructor
public class AttendanceService extends BaseCrudService<Attendance, Long> {

    private final AttendanceRepository attendanceRepository;
    private final MemberRepository memberRepository;

    // -------------------------------------------------------------------------
    // Template Method – abstract step implementations
    // -------------------------------------------------------------------------

    @Override
    protected Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    protected Attendance findById(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));
    }

    @Override
    protected void performDelete(Long id) {
        attendanceRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Template Method – hook overrides
    // -------------------------------------------------------------------------

    @Override
    protected void validate(Attendance attendance) {
        if (attendance.getMember() == null) {
            throw new IllegalArgumentException("Attendance must be linked to a member");
        }
    }

    @Override
    protected void beforeSave(Attendance attendance) {
        if (attendance.getDate() == null) {
            attendance.setDate(LocalDate.now());
        }
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    @Transactional
    public Attendance markAttendance(Long memberId, String checkinTimeStr) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setDate(LocalDate.now());
        attendance.setCheckinTime(LocalTime.parse(checkinTimeStr));

        return create(attendance); // delegate to Template Method
    }

    @Transactional
    public Attendance markCheckout(Long memberId, String checkoutTimeStr) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        Attendance attendance = attendanceRepository
                .findTopByMemberIdAndDateAndCheckoutTimeIsNullOrderByCheckinTimeDesc(memberId, LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No open attendance record found for member id: " + memberId + " on today's date"));

        LocalTime checkoutTime = LocalTime.parse(checkoutTimeStr);
        if (checkoutTime.isBefore(attendance.getCheckinTime())) {
            throw new IllegalArgumentException("checkoutTime cannot be earlier than checkinTime");
        }

        attendance.setCheckoutTime(checkoutTime);
        return save(attendance);
    }

    public List<Attendance> getAttendanceByMember(Long memberId) {
        return attendanceRepository.findByMemberIdOrderByDateDesc(memberId);
    }

    public long getTotalAttendanceCount() {
        return attendanceRepository.count();
    }
}
