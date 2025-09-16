package org.egov.lg.mcp.ccrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.egov.lg.mcp.ccrs.models.ComplaintSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CourseApplicationTests {

		@Autowired
		private UrbanMCPService tool;

		@Test
		public void testGetComplaints() {
			List<ComplaintSummary> result = tool.getComplaints("2025-04-01", "2025-08-31", "pb.amritsar", "0", "10");
			assertNotNull(result);
			assertFalse(result.isEmpty());
		}

		@Test
		public void testGetComplaintCategories() throws JsonProcessingException {
			JsonNode result = tool.fetchComplaintCategories("statea");
			System.out.println(result);
			assertNotNull(result);
			assertFalse(result.isEmpty());
		}

}
