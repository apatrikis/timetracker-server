option explicit

dim basedir
dim baseDate

basedir = Replace(WScript.ScriptFullName, WScript.ScriptName, "")
baseDate = CDate(#4/1/2015#)

dim fso, prjFile, empFile, prj2empFile, emp2roleFile, tbFile

Set fso = CreateObject("Scripting.FileSystemObject")
Set prjFile = fso.OpenTextFile(baseDir & "project.sql", 2, true)
Set empFile = fso.OpenTextFile(baseDir & "employee.sql", 2, true)
Set prj2empFile = fso.OpenTextFile(baseDir & "project2employee.sql", 2, true)
Set emp2roleFile = fso.OpenTextFile(baseDir & "employee2role.sql", 2, true)
Set tbFile = fso.OpenTextFile(baseDir & "timebooking.sql", 2, true)

dim prjMax, empPerPrj, tbPerEmp

prjMax = 100
empPerPrj = 10
tbPerEmp = 200

generateData

prjFile.Close
empFile.Close
prj2empFile.Close
emp2roleFile.Close
tbFile.Close

function generateData()
	dim prj
	for prj = 1 to prjMax
		printPrj prj
		printEmp prj
		printEmp2Role prj, "MANAGER"
		
		dim emp
		for emp = 1 to empPerPrj
			dim idxEmp
			idxEmp = prjMax + emp + ((prj - 1) * empPerPrj) ' offset MANAGER, plus current index, plus previously generated
			
			printEmp idxEmp
			printEmp2Role idxEmp, "USER"
			printPrj2Emp prj, idxEmp
			
			dim tb
			for tb = 1 to tbPerEmp
				printTb tb, prj, idxEmp
			next
		next
	next
end function

function printPrj(idxPrj)
	call prjFile.WriteLine("INSERT INTO PROJECT (PROJECTID, TITLE, DESCRIPTION, OWNER_EMAIL, STARTDATE, ENDDATE, LOCKED) VALUES ('Proj" & idxPrj & "', 'Title Proj" & idxPrj & "', 'Description of Proj" & idxPrj &"', 'emp.no" & idxPrj & "@tt.com', {d '2015-01-01'}, {d '2016-12-31'}, 0);")
end function

function printEmp(idxEmp)
	' passwd = secret
	empFile.WriteLine("INSERT INTO EMPLOYEE (FIRSTNAME, LASTNAME, EMAIL, PASSWORD) VALUES ('emp', 'no" & idxEmp & "', 'emp.no" & idxEmp & "@tt.com', '2bb80d537b1da3e38bd30361aa855686bde0eacd7162fef6a25fe97bf527a25b');")
end function

function printPrj2Emp(idxPrj, idxEmp)
	prj2empFile.WriteLine("INSERT INTO PROJECT2EMPLOYEE (ID, EMPLOYEE_EMAIL, PROJECT_PROJECTID) VALUES ('P2E-" & idxEmp & "', 'emp.no" & idxEmp & "@tt.com', 'Proj" & idxPrj & "');")
end function

function printEmp2Role(idxEmp, empRole)
	emp2roleFile.WriteLine("INSERT INTO EMPLOYEE2ROLE (ID, ROLENAME, EMPLOYEE_EMAIL) VALUES ('E2R-" & idxEmp & "', '" & empRole & "', 'emp.no" & idxEmp & "@tt.com');")
end function

function printTb(idxTb, idxPrj, idxEmp)
	dim bookDate
	bookDate = DateAdd("d", idxTb, baseDate)
	
	dim y, m, d, dateString
	y = Year(bookDate)
	m = Month(bookDate)
	d = Day(bookDate)
	dateString = "" & y & "-" & Right("00" & m, 2) & "-" & Right("00" & d, 2)
	tbFile.WriteLine("INSERT INTO TIMERECORD (ID, OWNER_EMAIL, PROJECT_PROJECTID, STARTTIME, ENDTIME, PAUSEMINUTES, RECORDSTATUS) VALUES ('TB-" & idxPrj & "-" & idxEmp & "-" & idxTb & "', 'emp.no" & idxEmp & "@tt.com', 'Proj" & idxPrj & "', {ts '" & dateString & " 09:00:00.0'}, {ts '" & dateString & " 18:00:00.0'}, 60, 1);")
end function
