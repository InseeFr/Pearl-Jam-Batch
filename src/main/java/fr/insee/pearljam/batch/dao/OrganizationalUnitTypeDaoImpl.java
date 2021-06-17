package fr.insee.pearljam.batch.dao;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.context.OrganizationalUnitType;
import fr.insee.pearljam.batch.dto.OrganizationUnitDto;

/**
 * Service for the  OrganizationalUnit entity that implements the interface associated
 * @author scorcaud
 *
 */
@Service
public class OrganizationalUnitTypeDaoImpl implements OrganizationalUnitTypeDao{
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Override
	public boolean existOrganizationalUnit(String id){
		String qString = "SELECT COUNT(id) FROM organization_unit WHERE id=?";
		Long nbRes = jdbcTemplate.queryForObject(qString, new Object[]{id}, Long.class);
		return nbRes>0;	
	}

	@Override
	public void createOrganizationalUnit(OrganizationalUnitType organizationalUnit) {
		String qString = "INSERT INTO public.organization_unit(id, label, type) VALUES (?, ?, ?)";
		jdbcTemplate.update(qString, organizationalUnit.getId(), organizationalUnit.getLabel(), organizationalUnit.getType());
	}
	
	@Override
	public void createOrganizationalUnitFromDto(OrganizationUnitDto organizationalUnit) {
		String qString = "INSERT INTO public.organization_unit(id, label, type, organization_unit_parent_id) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(qString, organizationalUnit.getCodeEtab(), organizationalUnit.getNomEtab(), "LOCAL", organizationalUnit.getOrigineEtab());
	}

	@Override
	public void updateOrganizationalUnitParent(String organizationalUnitChild, String organizationalUnitId) {
		String updateQuery = "UPDATE organization_unit SET organization_unit_parent_id = ? where id = ?";
		jdbcTemplate.update(updateQuery, organizationalUnitId, organizationalUnitChild);
	}

	@Override
	public boolean existOrganizationalUnitAlreadyAssociated(List<String> organizationalUnitRefs) {
		String inSql = String.join(",", Collections.nCopies(organizationalUnitRefs.size(), "?"));
		String qString =String.format("SELECT COUNT(id) FROM organization_unit WHERE id IN (%s) AND organization_unit_parent_id IS NOT NULL", inSql);
		Long nbRes = jdbcTemplate.queryForObject(qString, organizationalUnitRefs.toArray(), Long.class);
		return nbRes>0;	
	}

	@Override
	public List<String> findChildren(String currentOu) {
		String qString = "SELECT ou.id FROM organization_unit ou WHERE ou.organization_unit_parent_id=?";
		return jdbcTemplate.queryForList(qString, new Object[]{currentOu}, String.class);
	}

	@Override
	public boolean existOrganizationUnitNational(List<String> organizationalUnitRefs) {
		String inSql = String.join(",", Collections.nCopies(organizationalUnitRefs.size(), "?"));
		String qString =String.format("SELECT COUNT(id) FROM organization_unit WHERE id IN (%s) AND type='NATIONAL'", inSql);
		Long nbRes = jdbcTemplate.queryForObject(qString, organizationalUnitRefs.toArray(), Long.class);
		return nbRes>0;	
	}

}
