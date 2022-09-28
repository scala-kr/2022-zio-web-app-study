# Goal
간단한 TODO Web Server 만들기

## Tasks
- [x] hello 텍스트 출력하는 웹서버 띄우기
- [x] 하드코딩된 TODO 리스트 출력
- [x] in memory TODO 등록
    - [x] factor out repository
- [ ] postgresql repository
  - [ ] docker test container
  - [ ] flyway schema 
  - [ ] quill query

### Future tasks
- [ ] 설정파일
- [ ] 실제 데이터베이스
- [ ] logging (after zio-http 2.0.0-RC11)
 
- [ ] docker 등 패키징
 

## Features
- TODO 등록하기
- TODO 목록 보기
- TODO 한 개만 보기
- TODO 삭제하기

- TODO에 due date 추가
- ...