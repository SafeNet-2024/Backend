-- 현재 프론트에서 region을 등록하는 과정이 구현이 안 되었기에, 임의의 값을 넣어놓습니다.
-- 현제 데이터가 많이 없는 관계로 통일성 있게 넣어줍니다.
update member set region_id = 1 where member_id = 5;
update member set region_id = 1 where member_id = 6;
update member set region_id = 1 where member_id = 7;
update member set region_id = 356 where member_id = 8;
update member set region_id = 356 where member_id = 9;

-- 기존에 등록된 post는 지역 정보 넣기
update post set region_id = 1 where member_id = 5;
update post set region_id = 1 where member_id = 6;
update post set region_id = 356 where member_id = 9;
