package com.qiaopi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiaopi.context.UserContext;
import com.qiaopi.dto.QuestionSubmitDTO;
import com.qiaopi.entity.QuestionUserStatus;
import com.qiaopi.entity.Questions;
import com.qiaopi.entity.User;
import com.qiaopi.exception.QuestionException;
import com.qiaopi.mapper.QuestionsMapper;
import com.qiaopi.mapper.QuestionUserStatusMapper;
import com.qiaopi.mapper.UserMapper;
import com.qiaopi.service.QuestionService;
import com.qiaopi.utils.AESUtil;
import com.qiaopi.utils.MessageUtils;
import com.qiaopi.vo.GenQuestionVO;
import com.qiaopi.vo.QuestionAnswerVO;
import com.qiaopi.vo.QuestionSubmitVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor //自动注入
public class QuestionServiceImpl implements QuestionService {


    @Value("${aes.secret.key}")
    private String secretKey;

    private final QuestionUserStatusMapper questionUserStatusMapper;
    private final QuestionsMapper questionsMapper;
    private final UserMapper userMapper;

/*

    @Override
    public GenQuestionVO genQuestion(Long userChooseSetId) {
        //获取到当前用户的id
        Long currentId = UserContext.getUserId();

        // 查询当前线程用户的答题情况
        QueryWrapper<QuestionUserStatus> queryWrapper = new QueryWrapper<>();
        // 设置查询条件，user_id 字段等于传入的 userId
        queryWrapper.eq("user_id", currentId);
        QuestionUserStatus userQuestionInfoInTable = questionUserStatusMapper.selectOne(queryWrapper);


        if (userQuestionInfoInTable == null) {
            //如果该用户是第一次答题，需要根据用户id在question_user_status库中创建用户信息，并且给用户返回第一套题目

            // 根据用户id创建用户数据并存储到数据库中
            QuestionUserStatus newQuestionUserStatus = new QuestionUserStatus();
            newQuestionUserStatus.setUserId(currentId);
            questionUserStatusMapper.insert(newQuestionUserStatus);

            //返回题目 给用户返回question库中的第一套题目
            QueryWrapper<Questions> queryWrapper1 = new QueryWrapper<>();
            // 设置条件：set_id = 1
            queryWrapper1.eq("set_id", 1);
            List<Questions> result = questionsMapper.selectList(queryWrapper1);

            //创建genQuestionVO对象
            GenQuestionVO genQuestionVO = new GenQuestionVO();
            genQuestionVO.setQuestions(result);
           // genQuestionVO.setSetId(1L);
            return genQuestionVO;
        }




      */
/*  int questionSet1 = userQuestionInfoInTable.getQuestionSet1();
        int questionSet2 = userQuestionInfoInTable.getQuestionSet2();
        int questionSet3 = userQuestionInfoInTable.getQuestionSet3();
        int questionSet4 = userQuestionInfoInTable.getQuestionSet4();
        int questionSet5 = userQuestionInfoInTable.getQuestionSet5();
        int questionSet6 = userQuestionInfoInTable.getQuestionSet6();
        int questionSet7 = userQuestionInfoInTable.getQuestionSet7();
        int questionSet8 = userQuestionInfoInTable.getQuestionSet8();
        int questionSet9 = userQuestionInfoInTable.getQuestionSet9();
        int questionSet10 = userQuestionInfoInTable.getQuestionSet10();

        ArrayList<Integer> questionSet = new ArrayList<Integer>();
        questionSet.add(questionSet1);
        questionSet.add(questionSet2);
        questionSet.add(questionSet3);
        questionSet.add(questionSet4);
        questionSet.add(questionSet5);
        questionSet.add(questionSet6);
        questionSet.add(questionSet7);
        questionSet.add(questionSet8);
        questionSet.add(questionSet9);
        questionSet.add(questionSet10);
*//*

        //获取用户的答题情况，并且返回用户下一步需要回答的题目的套数
        ArrayList<Integer> questionSet = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            try {
                // 通过反射获取 getQuestionSetX 方法
                Method method = userQuestionInfoInTable.getClass().getMethod("getQuestionSet" + i);
                // 调用方法并获取返回值
                int questionSetValue = (int) method.invoke(userQuestionInfoInTable);
                // 将结果添加到列表
                questionSet.add(questionSetValue);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();  // 处理可能的异常
            }
        }



        int updatedValue = -1; // 用于存储加一后的值

        // 遍历集合，查找第一个值为0的变量
        for (int i = 0; i < questionSet.size(); i++) {
            if (questionSet.get(i) == 0) {
                // 将找到的值加1
                updatedValue = i + 1;
                //questionSet.set(i, 1); // 将集合中的值设为1表示已完成
                break; // 找到第一个符合条件的值后，结束循环
            }
        }

        System.out.println(updatedValue);

        // 输出找到的最小未完成的套题
        if (updatedValue != -1) {
            //找出需要提供给用户的题目的套数id
            System.out.println("找到第一个未完成的套题，编号为: " + updatedValue);

            //return new GenQuestionVO(questionList, (long) updatedValue);

        } else {

            System.out.println("所有套题均已完成。");
        }
        return null;
    }

*/



    @Override
    public ArrayList<Integer> userLoginPage() {
        Long currentId = null;
        try {
            //获取到当前用户的id
            currentId = UserContext.getUserId();
        } catch (Exception e) {
            throw new QuestionException(MessageUtils.message("user.getCurrentId.failed"));
        }

        // 查询当前线程用户的答题情况
        QueryWrapper<QuestionUserStatus> queryWrapper = new QueryWrapper<>();
        // 设置查询条件，user_id 字段等于传入的 userId
        queryWrapper.eq("user_id", currentId);
        QuestionUserStatus userQuestionInfoInTable = questionUserStatusMapper.selectOne(queryWrapper);

        if (userQuestionInfoInTable == null) {
            //如果该用户是第一次答题，需要根据用户id在question_user_status库中创建用户信息，并且给用户返回第一套题目

            // 根据用户id创建用户数据并存储到数据库中
            QuestionUserStatus newQuestionUserStatus = new QuestionUserStatus();
            //newQuestionUserStatus.setUserId(currentId);
            newQuestionUserStatus.setUserId(currentId);
            questionUserStatusMapper.insert(newQuestionUserStatus);

            ArrayList<Integer> gamePaperNeedToShowId = new ArrayList<>(Collections.nCopies(10, 0)); // 初始化列表，长度为10，所有元素为0
            // 设置下标为0的位置为1
            gamePaperNeedToShowId.set(0, 1);
            return gamePaperNeedToShowId;
        }
/*

        int questionSet1 = userQuestionInfoInTable.getQuestionSet1();
        int questionSet2 = userQuestionInfoInTable.getQuestionSet2();
        int questionSet3 = userQuestionInfoInTable.getQuestionSet3();
        int questionSet4 = userQuestionInfoInTable.getQuestionSet4();
        int questionSet5 = userQuestionInfoInTable.getQuestionSet5();
        int questionSet6 = userQuestionInfoInTable.getQuestionSet6();
        int questionSet7 = userQuestionInfoInTable.getQuestionSet7();
        int questionSet8 = userQuestionInfoInTable.getQuestionSet8();
        int questionSet9 = userQuestionInfoInTable.getQuestionSet9();
        int questionSet10 = userQuestionInfoInTable.getQuestionSet10();

        ArrayList<Integer> questionSet = new ArrayList<Integer>();
        questionSet.add(questionSet1);
        questionSet.add(questionSet2);
        questionSet.add(questionSet3);
        questionSet.add(questionSet4);
        questionSet.add(questionSet5);
        questionSet.add(questionSet6);
        questionSet.add(questionSet7);
        questionSet.add(questionSet8);
        questionSet.add(questionSet9);
        questionSet.add(questionSet10);
*/

        // 使用循环提取问题集
        ArrayList<Integer> questionSet = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            try {
                // 通过反射获取相应的方法并调用
                Method method = userQuestionInfoInTable.getClass().getMethod("getQuestionSet" + i);
                int questionSetValue = (int) method.invoke(userQuestionInfoInTable);
                questionSet.add(questionSetValue);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new QuestionException(MessageUtils.message("question.getSetId.failed"));
            }
        }
        // 返回 completedSetIds 中的套题ID，即用户已完成的套题
        return questionSet;
    }

    @Override
    public GenQuestionVO startAnswer(int setId) {
        //根据套id查询套中的题目
        QueryWrapper<Questions> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("set_id", setId);
        queryWrapper2.select("id", "set_id", "content", "option_a", "option_b", "option_c", "option_d");
        List<Questions> questionList = questionsMapper.selectList(queryWrapper2);

        //打乱题目顺序
        Collections.shuffle(questionList);
        GenQuestionVO genQuestionVO = new GenQuestionVO(questionList);

        return genQuestionVO;
    }



    @Override
    public QuestionSubmitVO submitAnswers(List<QuestionSubmitDTO> questionSubmitDTOs) {
        // 获取当前用户的 ID
        Long currentId = UserContext.getUserId();
        List<QuestionAnswerVO> questionAnswerVOList = new ArrayList<>(); // 题目答案对象列表
        int record = 0; // 记录用户答题正确数目

        for (QuestionSubmitDTO questionSubmitDTO : questionSubmitDTOs) {
            Long questionId = questionSubmitDTO.getQuestionId(); // 获取题目 ID
            String userAnswer = questionSubmitDTO.getSelectedOption(); // 获取用户选择的答案

            // 根据 ID 查询题目
            Questions question = questionsMapper.selectById(questionId);
            if (question == null) {
                throw new RuntimeException("题目未找到");
            }

            // 比较用户答案与正确答案
            boolean isCorrect = userAnswer.equalsIgnoreCase(question.getCorrectAnswer());
            if (isCorrect) {
                record++;
            }

            // 封装题目答案信息
            QuestionAnswerVO questionAnswerVO = new QuestionAnswerVO();
            questionAnswerVO.setQuestionId(questionId);
            questionAnswerVO.setContent(question.getContent());
            questionAnswerVO.setOptionA(question.getOptionA());
            questionAnswerVO.setOptionB(question.getOptionB());
            questionAnswerVO.setOptionC(question.getOptionC());
            questionAnswerVO.setOptionD(question.getOptionD());
            questionAnswerVO.setCorrectAnswer(question.getCorrectAnswer());
            questionAnswerVO.setExplanation(question.getExplanation());
            questionAnswerVO.setUserAnswer(userAnswer);
            questionAnswerVO.setCorrect(isCorrect);

            questionAnswerVOList.add(questionAnswerVO);
        }

        int pigMoney = record * 5; // 计算积分

        User user = userMapper.selectById(currentId);
        Long currentMoney = user.getMoney();
        Long newMoney = currentMoney + pigMoney;
        // 更新用户的 money
        user.setMoney(newMoney);
        userMapper.updateById(user);

        // 如果全部答对，开启下一个篇章
        if (record == 10) {
            return handleCompleteAnswers(currentId, questionAnswerVOList, pigMoney,record);
        }

        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO(questionAnswerVOList, pigMoney,record);

        return questionSubmitVO;
        //return new QuestionSubmitVO(questionAnswerVOList, pigMoney);
    }


    private QuestionSubmitVO handleCompleteAnswers(Long currentId, List<QuestionAnswerVO> questionAnswerVOList, int pigMoney,Integer record) {
        QuestionAnswerVO firstQuestionAnswer = questionAnswerVOList.get(0);
        Long questionId = firstQuestionAnswer.getQuestionId(); // 根据题目 ID 查询对应的表 ID
        Questions questions = questionsMapper.selectById(questionId); // 根据表 ID 获取到题目
        Long setId = questions.getSetId(); // 获取题目集 ID

        long setIdNeedToUserId = setId + 1; // 下一个篇章的 setId

        // 返回用户需要的全部问题
        QueryWrapper<Questions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("set_id", setIdNeedToUserId);
        List<Questions> needToGiveUserQuestionList = questionsMapper.selectList(queryWrapper);

        // 封装到返回对象中
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO(questionAnswerVOList, pigMoney, needToGiveUserQuestionList,record);

        // 更新用户问题的状态
        updateUserQuestionStatus(currentId, setId);

        return questionSubmitVO;
    }

    private void updateUserQuestionStatus(Long currentId, Long setId) {
        // 查询用户状态
        QueryWrapper<QuestionUserStatus> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentId);
        QuestionUserStatus questionUserStatus = questionUserStatusMapper.selectOne(queryWrapper);

        // 更新用户问题的状态
        int setIdInt = setId.intValue(); // 将 Long 转换为 int
        switch (setIdInt) {
            case 1:
                questionUserStatus.setQuestionSet1(1);
                break;
            case 2:
                questionUserStatus.setQuestionSet2(1);
                break;
            case 3:
                questionUserStatus.setQuestionSet3(1);
                break;
            case 4:
                questionUserStatus.setQuestionSet4(1);
                break;
            case 5:
                questionUserStatus.setQuestionSet5(1);
                break;
            case 6:
                questionUserStatus.setQuestionSet6(1);
                break;
            case 7:
                questionUserStatus.setQuestionSet7(1);
                break;
            case 8:
                questionUserStatus.setQuestionSet8(1);
                break;
            case 9:
                questionUserStatus.setQuestionSet9(1);
                break;
            case 10:
                questionUserStatus.setQuestionSet10(1);
                break;
            default:
                throw new IllegalArgumentException("无效的题目集ID");
        }

        questionUserStatusMapper.updateById(questionUserStatus);
    }





    @Override
    public String allAnswerToFront(int setId) {
        // 创建 QueryWrapper 实例
        QueryWrapper<Questions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("set_id", setId);
        List<Questions> questionsList = questionsMapper.selectList(queryWrapper);


        AESUtil aesUtil = new AESUtil();
        //String secretKey = "1234567890123456";  // 16 字符的密钥

        try {
            //编码
            String encryptedData = aesUtil.encryptQuestions(questionsList, secretKey);
            log.info("加密后的数据：{}", encryptedData);
            return encryptedData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Questions> decode(String answer) {
        AESUtil aesUtil = new AESUtil();
        //String secretKey = "1234567890123456";  // 16 字符的密钥

        try {
            //编码
            List<Questions> questions = aesUtil.decryptQuestions(answer, secretKey);
            log.info("解密后的数据：{}", questions);
            return questions;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}




