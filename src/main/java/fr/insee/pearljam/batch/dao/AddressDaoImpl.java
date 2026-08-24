package fr.insee.pearljam.batch.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.campaign.InseeAddressType;

/**
 * Interface for the Address entity
 * 
 * @author scorcaud
 *
 */
@Service
public class AddressDaoImpl implements AddressDao {

	@Autowired
	@Qualifier("pilotageJdbcTemplate")
	JdbcTemplate pilotageJdbcTemplate;

	@Override
	public Long createAddress(InseeAddressType inseeAddress) {
		String qString = "INSERT INTO address (dtype, l1, l2, l3, l4, l5, l6, l7, building, floor, door, staircase, elevator, city_priority_district) VALUES ('InseeAddress',?,?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id";
		return pilotageJdbcTemplate.queryForObject(qString,
				new Object[] { inseeAddress.getL1(), inseeAddress.getL2(), inseeAddress.getL3(), inseeAddress.getL4(),
						inseeAddress.getL5(), inseeAddress.getL6(), inseeAddress.getL7(),
						inseeAddress.getBuilding(), inseeAddress.getFloor(), inseeAddress.getDoor(),
						inseeAddress.getStaircase(), inseeAddress.isElevator(), inseeAddress.isCityPriorityDistrict() },
				Long.class);
	}

	@Override
	public void updateAddress(InseeAddressType inseeAddress, String surveyUnitId) {
		String qString = """
				UPDATE public.address as adrs
				SET l1=?, l2=?, l3=?, l4=?, l5=?, l6=?, l7=?,
				building=?, floor=?, door=?, staircase=?, elevator=?, city_priority_district=?
				FROM survey_unit su
				WHERE su.address_id=adrs.id AND su.id=?
				""";

		pilotageJdbcTemplate.update(qString, inseeAddress.getL1(), inseeAddress.getL2(), inseeAddress.getL3(),
				inseeAddress.getL4(), inseeAddress.getL5(), inseeAddress.getL6(), inseeAddress.getL7(),
				inseeAddress.getBuilding(), inseeAddress.getFloor(), inseeAddress.getDoor(),
				inseeAddress.getStaircase(), inseeAddress.isElevator(), inseeAddress.isCityPriorityDistrict(),
				surveyUnitId);
	}

	public InseeAddressType getAddressBySurveyUnitId(String surveyUnitId) {
		String qString = "SELECT * FROM address ad INNER JOIN survey_unit su on su.address_id = ad.id WHERE su.id=?";
		List<InseeAddressType> addList = pilotageJdbcTemplate.query(qString, new Object[] { surveyUnitId },
				new AddressMapper());
		if (!addList.isEmpty()) {
			return addList.get(0);
		} else {
			return null;
		}
	}

	private static final class AddressMapper implements RowMapper<InseeAddressType> {
		public InseeAddressType mapRow(ResultSet rs, int rowNum) throws SQLException {
			InseeAddressType in = new InseeAddressType();
			in.setL1(rs.getString("l1"));
			in.setL2(rs.getString("l2"));
			in.setL3(rs.getString("l3"));
			in.setL4(rs.getString("l4"));
			in.setL5(rs.getString("l5"));
			in.setL6(rs.getString("l6"));
			in.setL7(rs.getString("l7"));
			in.setBuilding(rs.getString("building"));
			in.setFloor(rs.getString("floor"));
			in.setDoor(rs.getString("door"));
			in.setStaircase(rs.getString("staircase"));
			boolean uncheckedAscenceur = rs.getBoolean("elevator");
			in.setElevator(rs.wasNull() ? null : uncheckedAscenceur);
			boolean uncheckedQPV = rs.getBoolean("city_priority_district");
			in.setCityPriorityDistrict(rs.wasNull() ? null : uncheckedQPV);
			return in;
		}
	}

	public void deleteAddressById(Long addressId) {
		String qString = "DELETE FROM address WHERE id=?";
		pilotageJdbcTemplate.update(qString, addressId);
	}
}
