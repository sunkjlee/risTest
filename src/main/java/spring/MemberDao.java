package spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;


public class MemberDao {

	//spring jdbc의 jdbcTemplate class사용 해서 db 처리 
	private JdbcTemplate jdbcTemplate;
	
	//DataSource : 데이터베이스 접속 오브젝트인 connetion 오브젝트의 팩토리 패턴
	//커넥션 오브젝트의 생성과 소멸 시의 부하를 줄이는것이 목적..
	//JdbcTemplate 생성자
	public MemberDao(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public Member selectByEmail(String email) {
		/* 		
		// 추상클래스 RowMapper<Member>를 이용하지 않고 RowMapper구현 클래스를(MemberRowMapper) 이용하는 경우
		List<Member> results1 =  jdbcTemplate.query( 
				"select * from MEMBER where EMAIL = ?",
				//MemberRowMapper 객체 생성, MemberRowMapper실 객체를 던져주면 MemberRowMapper클래스의 mapRow 메소드를 실행해 meber 클래스를 전달해줌
				//이를 위해서는 추상클래스의 구현 클래스 작성시 추상메서드인  mapRow를 반드시 실 구현해줘야 함.(안해주면 오류나서 어차피 안됨)
				 new MemberRowMapper(),
				 email);
		*/
		
		List<Member> results = jdbcTemplate.query( 
				"select * from MEMBER where EMAIL = ?",
				//임의 클래스를RowMapper<Member> 이용해서 RowMapper의 객체를 전달
				//RowMapper는 ResultSet에서 데이터를 읽어서 Member 객체로 변환해주는 기능을 제공
				new RowMapper<Member>() {
					@Override
					//추상 클래스를의 추상 메소드 임시 구현(mapRow메서드를 임시 구현함)
					//mapRow : Sql실행결과로 구한 ResultSet으로부터  data를 읽어 자바 객체로 변환해주는 매퍼 역활
					//Member객체를 생성한뒤 rs의 data를 변환하고, member객체를 리턴
					public Member mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						//member 객체 생성 및 ResultSet의 내용 전달 
						//Member 클래스의 생성자 메서드 수행됨
						Member member = new Member(rs.getString("EMAIL"),
								rs.getString("PASSWORD"),
								rs.getString("NAME"),
								rs.getTimestamp("REGDATE"));
						member.setId(rs.getLong("ID"));
						
						//data생성한후 member 객체 리턴
						return member;
					}
				},
				email);  //물음표에 해당하는 값 전달

		return results.isEmpty() ? null : results.get(0);
	}
	
	public List<Member> selectAll() {
		List<Member> results = jdbcTemplate.query("select * from MEMBER",
				new RowMapper<Member>() {
					@Override
					public Member mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						
						Member member = new Member(rs.getString("EMAIL"),
								rs.getString("PASSWORD"),
								rs.getString("NAME"),
								rs.getTimestamp("REGDATE"));
						member.setId(rs.getLong("ID"));
						return member;
					}
				});
		return results;
	}

	public void insert(final Member member) {
		//insert된 member객체의 id값을 구할때 사용 
		KeyHolder keyHolder = new GeneratedKeyHolder();
		/* PreparedStatement의 set 메서드 사용시 코드
		  PreparedStatementCreator객체의 createPreparedStatement 메소드이용해서  PreparedStatement 생성
		   여기서는 추상 클래스이용해서 임스 클래스로 구현
		*/		
						
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) 
					throws SQLException {
				PreparedStatement pstmt = con.prepareStatement(
						"insert into MEMBER (ID, EMAIL, PASSWORD, NAME, REGDATE) "
						+ "values (seq_memberId.NEXTVAL,?, ?, ?, ?)",
						new String[] {"ID"});		//member객체의 id값 받기위한 인자 
				pstmt.setString(1,  member.getEmail());
				pstmt.setString(2,  member.getPassword());
				pstmt.setString(3,  member.getName());
				pstmt.setTimestamp(4,  
						new Timestamp(member.getRegisterDate().getTime()));
				return pstmt;
			}
		}, keyHolder);
		Number keyValue = keyHolder.getKey();
		member.setId(keyValue.longValue());
		
		/*
		   // 일반 식으로 할때..
		jdbcTemplate.update( "insert into MEMBER (ID, EMAIL, PASSWORD, NAME, REGDATE) "
					+ "values ( seq_memberId.NEXTVAL,?, ?, ?, ?)"
					, member.getEmail(), member.getPassword(), member.getName()
					, new Timestamp(member.getRegisterDate().getTime()));
		*/ 
		
	}

	public void update(Member member) {
		jdbcTemplate.update("update MEMBER set NAME = ?, PASSWORD = ? where EMAIL = ?",
				member.getName(), member.getPassword(), member.getEmail());
	}

	public int count() {
		// 해당 sql쿼리 결과가 list가 아니랄 단건일(한행일) 경우 queryForObject 를 사용 
		//query메소드와 차이는 리턴타입이 List가 아니라 RowMapper가 변환해주는 타입이라는 점
		//여기서는 Integer, 2개 이상의 컬럼일 경우 meber처럼 객체를 타입을 지정해서 , mapRow 메서드 구현해서 처리 해도됨(jdbcTemplate.query 처럼)
	    //그니까 한행일 경우만 사용한다는 것..
		Integer count = jdbcTemplate.queryForObject("select count(*) from MEMBER",
				//컬럼을 읽어올때 사용할 타입 지정
				Integer.class);
		return count;
	}

	
}
