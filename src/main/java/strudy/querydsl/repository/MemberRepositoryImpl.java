package strudy.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import strudy.querydsl.dto.MemberSearchCondition;
import strudy.querydsl.dto.MemberTeamDto;
import strudy.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.*;
import static org.springframework.util.StringUtils.isEmpty;
import static strudy.querydsl.entity.QMember.member;
import static strudy.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em){
        this.queryFactory=new JPAQueryFactory(em);
    }


    @Override
        public List<MemberTeamDto> search(MemberSearchCondition condition){
            return queryFactory
                    .select(new QMemberTeamDto(
                            member.id,
                            member.username,
                            member.age,
                            team.id,
                            team.name))
                    .from(member)
                    .leftJoin(member.team, team)
                    .where(
                            usernameEq(condition.getUsername()),
                            teamNameEq(condition.getTeamName()),
                            ageGoe(condition.getAgeGoe()),
                            ageLoe(condition.getAgeLoe())
                    )
                    .fetch();
        }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        team.id,
                        team.name))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
//                .fetchResults();//컨텐츠용 쿼리 1번, count용 쿼리 한번 씩 각각 날려서 정보도 확인 가능함!

        long size = content.size();

        return new PageImpl<>(content,pageable,size);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        return null;
    }

//    @Override
//    public List<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
//        return null;
//    }




    private BooleanExpression usernameEq(String username) {
            return hasText(username) ?  member.username.eq(username):null;
        }
        private BooleanExpression teamNameEq(String teamName) {
            return hasText(teamName) ?team.name.eq(teamName):null;
        }
        private BooleanExpression ageGoe(Integer ageGoe) {
            return ageGoe == null ? null : member.age.goe(ageGoe);
        }
        private BooleanExpression ageLoe(Integer ageLoe) {
            return ageLoe == null ? null : member.age.loe(ageLoe);
        }

}
