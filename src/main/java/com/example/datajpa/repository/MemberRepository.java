package com.example.datajpa.repository;

import com.example.datajpa.domain.Member;
import com.example.datajpa.dto.MemberDto;
import com.example.datajpa.repository.projection.MemberProjection;
import com.example.datajpa.repository.projection.NameOnly;
import com.example.datajpa.repository.projection.UsernameOnlyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {//첫번째 타입, 두번째 매핑된 pk

    /*
    * JpaRepository<T , ID> 타입과 PK
    * spring data 프로젝트가 공통된 crud를 제공하고, jpaRepository는 jpa특화 기능을 제공한다.
    * (paging, sorting같은 공통적인건 crudRepository가 제공한다는뜻)
    * crudRepository : 기본적인거 findBy(~) 등 기타등등 메소드들이 들어있다.
    * 최상위 단
    * Repository<T,ID> spring bean을 만들때 classPath를 쉽게 찾을 수 있도록 만들어준다.
    *
    * Repository <- crudRepository <- pagingAndSortingRepository <- JpaRepository
    *
    * 사용자 정의 인터페이스
    * 1. 인터페이스 하나 만든다(이름은 자유)
    * 2. 인터페이스 구현체를 만든다. 구현체의 이름은 사용하길 원하는 jpa인터페이스 이름 + Impl이다.
    *       이름 규칙을 맞춰줘야. Spring Data Jpa가 찾아서 호출 해준다.
    * 3. 사용하길 원하는 jpa 인터페이스에서 인터페이스를 상속받는다.
    *
    *  Auditing
    *  등록일 , 수정일 , 등록자, 수정자
    * */

    List<Member> findByNameAndAgeGreaterThan(String username, int age);

    List<Member> findTop3ABy();

//    @Query(name = "Member.findByName")
    List<Member> findByName(@Param("name") String name);

    @Query("select m from Member m where m.name = :name and m.age = :age")
    List<Member> findUser(@Param("name") String name, @Param("age") int age);

    @Query("select m.name from Member m")
    List<String> findNameList();

    @Query("select new com.example.datajpa.dto.MemberDto(m.id, m.name, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.name in :names")
    List<Member> findByNames(@Param("names") List<String> names);


    @Query("select m from Member m where m.name in :names")
    Optional<Member> findOneByName(@Param("names") String names);


//    @Query(value = "select m from Member m" , countQuery = "select count(m.name) from Member m") //count쿼리는 성능 이슈가 있을 수 있어서 따로 날릴 수 있다!
    Page<Member> findByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true) // clearAutomatically --> flush, clear자동으로 날려준다.
    @Query(value = "update members m set m.age = m.age +1 where m.age >= :age" , nativeQuery = true) //벌크 연산에서는 테이블 대상으로 쿼리를 날려야 한다.
    int bulkAgePlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> getMemberByFetch();

    @Override
    @EntityGraph(attributePaths = ("team"))//jpql로 모두 fetchjoin해야 하는 수고를 덜어준다.
    List<Member> findAll();


//    @EntityGraph("member.all")
    @EntityGraph(attributePaths = ("team"))
    @Query("select m from Member m where m.name =:name")
    List<Member> findByNameDefault(@Param("name") String name);

    @QueryHints(value = @QueryHint( name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByName(String name);


    //select for update --> pessimistic lock
    //@Version --> optimistic lock도 존재
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Member findLockByName(String name);

    List<NameOnly> findProjectionByName(@Param("name")String name);
    //반환 타입에다가 내가 원하는 필드 넣으면 끝!

    List<UsernameOnlyDto> findProjection2ByName(@Param("name") String name);

    //안에다가 원하는 타입 ex(usernameOnlyDto)같이 넣어주면 동적으로 projection이 가능하다.
    <T> List<T> findProjection3ByName(@Param("name") String name, Class<T> type);

    @Query(value = "select * from members where name = ?", nativeQuery = true)
    Member findByNativeQuery(String name);
    /*
    * 왠만하면 사용하지 말것..
한계점이 많음, select 할 때 특정 애들을 전부 적어줘야 한다.
문제는 반환타입이 몇가지 지정이 안된다. ex) username만 보고싶을 경우. --> mybatis 엮어서 쓰던지 하자.
* sort 파라미터 작동하지 않을 수도 있다.
* jpql처럼 로딩 시점에 쿼리를 확인 불가능하다.
* 동적 쿼리가 불가능 하다.
* projection을 활용하면 보완가능.
* */
    @Query(value = "select m.member_id as id, m.name , t.name as teamName " +
            "from members m left join team t",
            countQuery = "select count(*) from members"
            , nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);
}