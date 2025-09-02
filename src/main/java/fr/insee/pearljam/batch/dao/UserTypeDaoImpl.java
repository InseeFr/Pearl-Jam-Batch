package fr.insee.pearljam.batch.dao;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.context.UserType;

@Service
@RequiredArgsConstructor
public class UserTypeDaoImpl implements UserTypeDao{
	JdbcTemplate pilotageJdbcTemplate;
	
	@Override
	public boolean existUser(String id) {
		String qString = "SELECT COUNT(id) FROM user WHERE id=?";
		Long nbRes = pilotageJdbcTemplate.queryForObject(qString, Long.class, id);
		return nbRes>0;	
	}
	
	@Override
	public void createUser(UserType user) {
		String qString = "INSERT INTO user (id, first_name, last_name)VALUES (?, ?, ?)";
		pilotageJdbcTemplate.update(qString, user.getId(), user.getFirstName(), user.getLastName());
	}

	@Override
	public void updateOrganizationalUnitByUserId(String userId, String id) {
		String updateQuery = "UPDATE user SET organization_unit_id = ? where id = ?";
		pilotageJdbcTemplate.update(updateQuery, id, userId);
	}

	@Override
	public boolean userAlreadyAssociated(List<String> userId, String organizationUnitId) {
		String qString ="SELECT COUNT(id) FROM user WHERE id IN (?) AND organization_unit_id IS NOT NULL AND organization_unit_id<>?";
		Long nbRes = pilotageJdbcTemplate.queryForObject(qString, new Object[]{String.join(",", userId), organizationUnitId}, Long.class);
		return nbRes>0;	
	}

	@Override
	public boolean userAlreadyAssociatedToOrganizationUnitId(String userId, String organizationalUnitId) {
		String qString ="SELECT COUNT(id) FROM user WHERE id=? AND organization_unit_id IS NOT NULL AND organization_unit_id=?";
		Long nbRes = pilotageJdbcTemplate.queryForObject(qString, new Object[]{userId, organizationalUnitId}, Long.class);
		return nbRes>0;	
	}

	@Override
	public List<String> findAllUsersWithoutOrganizationUnit() {
		String qString = "SELECT id FROM user WHERE organization_unit_id IS NULL";
		return pilotageJdbcTemplate.queryForList(qString, String.class);
	}
}
