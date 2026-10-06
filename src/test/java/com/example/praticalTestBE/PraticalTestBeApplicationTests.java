package com.example.praticalTestBE;

import com.example.praticalTestBE.dto.request.*;
import com.example.praticalTestBE.dto.response.FormResponse;
import com.example.praticalTestBE.dto.response.FormVersionResponse;
import com.example.praticalTestBE.service.FormService;
import com.example.praticalTestBE.service.FormVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PraticalTestBeApplicationTests {

	@Autowired
	private FormService formService;

	@Autowired
	private FormVersionService versionService;

	@Test
	void testSaveAndUpdateFormWorkflow() {
		String formCode = "TEST_FORM_" + System.currentTimeMillis();
		CreateFormRequest createReq = new CreateFormRequest(
			formCode,
			"Test Form",
			"Test Description",
			"General",
			List.of(),
			List.of()
		);
		FormResponse created = formService.createForm(createReq);
		assertNotNull(created.id());

		ComponentRequest textComp = new ComponentRequest(
			"emp_name", "Employee Name", com.example.praticalTestBE.domain.enums.ComponentType.TEXT,
			"Enter name", "", 1, true, true, List.of(), List.of(), List.of()
		);
		ComponentRequest numComp = new ComponentRequest(
			"emp_age", "Age", com.example.praticalTestBE.domain.enums.ComponentType.NUMBER,
			"Enter age", "", 2, false, true, List.of(), List.of(), List.of()
		);

		FormSectionRequest sectionReq = new FormSectionRequest(
			"sec_1", "Main Section", "Desc", 1, List.of(textComp, numComp)
		);

		UpdateFormRequest updateReq1 = new UpdateFormRequest(
			"Test Form Updated 1", "Desc 1", "General", List.of(sectionReq), List.of()
		);

		FormResponse updated1 = formService.updateForm(created.id(), updateReq1);
		assertNotNull(updated1);

		FormVersionResponse verDetails1 = versionService.getVersionDetails(created.id(), 1);
		assertEquals(1, verDetails1.sections().size());
		assertEquals(2, verDetails1.sections().get(0).components().size());

		// Second update (Save Configuration again)
		ComponentRequest dropdownComp = new ComponentRequest(
			"dept", "Department", com.example.praticalTestBE.domain.enums.ComponentType.DROPDOWN,
			"Select dept", "", 3, true, true,
			List.of(new ComponentOptionRequest("HR", "hr", 1, false), new ComponentOptionRequest("IT", "it", 2, false)),
			List.of(), List.of()
		);
		FormSectionRequest sectionReq2 = new FormSectionRequest(
			"sec_1", "Main Section", "Desc", 1, List.of(textComp, numComp, dropdownComp)
		);
		UpdateFormRequest updateReq2 = new UpdateFormRequest(
			"Test Form Updated 2", "Desc 2", "General", List.of(sectionReq2), List.of()
		);

		FormResponse updated2 = formService.updateForm(created.id(), updateReq2);
		assertNotNull(updated2);

		FormVersionResponse verDetails2 = versionService.getVersionDetails(created.id(), 1);
		assertEquals(1, verDetails2.sections().size());
		assertEquals(3, verDetails2.sections().get(0).components().size());
	}

}

