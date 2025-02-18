package sopio.acha.domain.memberActivity.application;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sopio.acha.domain.memberActivity.infrastructure.MemberActivityRepository;

@Service
@RequiredArgsConstructor
public class MemberActivityService {
	private final MemberActivityRepository memberActivityRepository;
}
